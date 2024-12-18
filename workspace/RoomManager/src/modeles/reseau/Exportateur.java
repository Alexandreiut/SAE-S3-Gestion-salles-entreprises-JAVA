/*
 * Exportateur.java					24/10/2024
 * BUT Info2, 2024/2025, pas de copyright
 */

package modeles.reseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.util.ArrayList;

import modeles.items.*;
import modeles.securite.Crypteur;
import modeles.sortie.EcritureCSV;
import modeles.stockage.Stockage;

/**
 * Représente un exportateur, avec un socket et un port, voulant 
 * faire une exportation distante
 */
public class Exportateur {
	
	/** 
	 * socket associé à l'exportateur permettant l'attente d'un client 
	 */
	private ServerSocket socketServeur;
	
	/** 
	 * socket associé à l'exportateur permettant la communication 
	 * avec le client 
	 */
	private Socket socketCommunication;
	
	/** 
	 * stockage de l'application 
	 */
	private Stockage stockage;
	
	/** 
	 * sortie de l'exportateur 
	 */
	private PrintWriter output;
	
	/**
	 * crypteur utilisé lors des échanges
	 */
	private Crypteur crypteur;
	
	/**
	 * instancie un exportateur et associe un socket de serveur à un port
	 * @param port port d'écoute du socket
	 * @param stockage stockage de lecture de l'exportateur
	 * @throws IOException s'il y a un problème lors de la création du socket
	 */
	public Exportateur(int port, Stockage stockage) throws IOException {
		
		InetAddress ip;
		ip = InetAddress.getLocalHost(); // valeur par défaut
		
		// recherche une adresse ip utilisable si l'ip par défaut n'est pas utilisable
        try {
        	
        	int i;
        	
        	Object[] tableauInterface = NetworkInterface
        			                    .networkInterfaces().toArray();
        	i = 0;
        	while (i < tableauInterface.length && !ip.isReachable(100)) {
        		
        		NetworkInterface interfaceReseau
				= (NetworkInterface) tableauInterface[i];
				
				
				// Vérifier si l'interface est active
			    if (interfaceReseau.isUp()) {
			        
			        // Obtenir les adresses IP associées à cette interface
			        for (InterfaceAddress adresseInterface :
			        	 interfaceReseau.getInterfaceAddresses()) {
			        	
			            InetAddress inetAddress = adresseInterface.getAddress();
			            
			            // Nous cherchons des adresses IPv4 uniquement
			            if (inetAddress instanceof Inet4Address) {
			                ip = inetAddress;
			            }
			        }
			    }
        		
        	}
        	
		} catch (SocketException e) {
			throw new IOException();
		}
		
		socketServeur = new ServerSocket(port, 1, ip);
		socketServeur.setSoTimeout(5000);
		socketServeur.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		
		this.stockage = stockage;
	}
	
	/**
	 * instancie un exportateur et associe un socket de serveur à un port
	 * @param port port d'écoute du socket
	 * @param stockage stockage de lecture de l'exportateur
	 * @param ip adresse ip du serveur
	 * @throws IOException s'il y a un problème lors de la création du socket
	 */
	public Exportateur(int port, Stockage stockage, InetAddress ip) throws IOException {
		
		socketServeur = new ServerSocket(port, 1, ip);
		socketServeur.setSoTimeout(5000);
		socketServeur.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		
		this.stockage = stockage;
	}
	
	/**
	 * accepte la connexion d'un client, instancie 
	 * socketCommunication et output et établie le cryptage
	 * afin de permettre la communication avec le client
	 * @throws IOException s'il y a erreur lors de la création 
	 * de la communication
	 */
	public void accepterConnexion() throws IOException {
		
		int clePubExp;
		int clePubImp;
		
		socketCommunication = socketServeur.accept();
		
		output = new PrintWriter(socketCommunication.getOutputStream(), true);
		
		crypteur = new Crypteur();
		clePubExp = crypteur.diffieHellman.getGPuissanceX();
		
		envoiMessage(Integer.toString(clePubExp));
		
		clePubImp = Integer.parseInt(recevoirMessage());
		
		crypteur.diffieHellman.calculeSecret(clePubImp);
		crypteur.genererCle();
	}
	
	/**
	 * envoi les données converties au format csv à l'importateur
	 */
	public void envoiDonnee() {
		envoiActivites();
		envoiSalles();
		envoiEmployes();
		envoiReservations();
	}
	
	/**
	 * envoi les données d'un fichier
	 * @param donneesAEnvoyer données du fichier à envoyer
	 */
	private void envoiFichier(ArrayList<String> donneesAEnvoyer) {
		
		int compteurOctets;
		String paquet;
		
		compteurOctets = 0;
		paquet = "";
		for (String ligne : donneesAEnvoyer) {
			
			compteurOctets += ligne.length();
			
			paquet += ligne + " ;sep:α; ";
			
			if (compteurOctets > 1000) {
				
				//envoi paquet crypté
				output.println(crypteur.crypteMessage(paquet));
				
				compteurOctets = 0;
				paquet = "";
			}
		}
		
		// flush de fin
		output.println(crypteur.crypteMessage(paquet));
		output.println(crypteur.crypteMessage("FIN"));
	}
	
	/**
	 * envoi les salles converties au format csv à l'importateur
	 */
	public void envoiSalles() {
		
		ArrayList<Salle> listeSalles = stockage.getListeSalle();
		
		ArrayList<String> donneesAEnvoyer;
		
		donneesAEnvoyer = EcritureCSV.ecrireSalles(listeSalles);
		
		envoiFichier(donneesAEnvoyer);
		
		
	}
	
	

	/**
	 * envoi les activités converties au format csv à l'importateur
	 */
	public void envoiActivites() {
		
		ArrayList<Activite> listeActivites = stockage.getListeActivite();
		
		ArrayList<String> donneesAEnvoyer;
		
		donneesAEnvoyer = EcritureCSV.ecrireActivites(listeActivites);
		
		envoiFichier(donneesAEnvoyer);
	}
	
	/**
	 * envoi les employés convertis au format csv à l'importateur
	 */
	public void envoiEmployes() {
		
		ArrayList<Employe> listeEmployes = stockage.getListeEmploye();
		
		ArrayList<String> donneesAEnvoyer;
		
		donneesAEnvoyer = EcritureCSV.ecrireEmployes(listeEmployes);
		
		envoiFichier(donneesAEnvoyer);
	}
	
	/**
	 * envoi les réservations converties au format csv à l'importateur
	 */
	public void envoiReservations() {
		
		ArrayList<Reservation> listeReservations = stockage.getListeReservation();
		
		ArrayList<String> donneesAEnvoyer;
		
		donneesAEnvoyer = EcritureCSV.ecrireReservations(listeReservations);
		
		envoiFichier(donneesAEnvoyer);
	}
	

	/**
	 * envoi le message au client
	 */
	public void envoiMessage(String valeur) {
        output.println(valeur);
	}
	
	
	/**
	 * reçois un message du client
	 * @return un message sous la forme d'une chaîne de caractères
	 * @throws IOException en cas d'erreur de lecture
	 */
	public String recevoirMessage() throws IOException {
		
		BufferedReader input;
		
		input = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
		
		return input.readLine();
		
	}
	
	
	/**
	 * @return true si la connexion avec l'importateur 
	 * a été correctement fermée,
	 * false sinon
	 */
	public boolean closeConnexion() {
		try {
			try {
				socketCommunication.close();
			} catch (NullPointerException e) {
				// cas dans lequel le socket est fermé
				// avant la connexion avec l'importateur
			}
			socketServeur.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
}

