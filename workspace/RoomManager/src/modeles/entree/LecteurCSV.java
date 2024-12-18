/*
 * LecteurCSV.java 						17/10/2024
 * BUT Info2, 2024/2025, pas de copyright
 */

package modeles.entree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import lanceur.RoomManager;
import modeles.erreur.LectureException;
import modeles.erreur.WrongFileFormatException;
import modeles.items.Activite;
import modeles.items.Employe;
import modeles.items.Reservation;
import modeles.items.Salle;

/**
 * Classe outil pour la lecture des csv
 * @author Adrien ASTIER, Noé ARCIER, Lucas BOULOUARD
 */
public class LecteurCSV {

	/**
	 * @param chemin le chemin du fichier
	 * @return Le contenu du fichier sous forme d'une liste de ligne.
	 * @throws WrongFileFormatException si l'extension du fichier 
	 * est incorrecte
	 * @throws IOException
	 */
	public static ArrayList<String> getRessource(String chemin) 
			throws WrongFileFormatException, IOException {
		ArrayList<String> listeLignes = new ArrayList<>();

		if (!getExtensionFichier(chemin).equals("csv")) {
			throw new WrongFileFormatException();
		}

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(chemin)))) {
			String ligne;
			while ((ligne = bufferedReader.readLine()) != null) {
				listeLignes.add(ligne);
			}
		}

		if (listeLignes.isEmpty()) {
			throw new IOException("Le fichier est vide.");
		}

		return listeLignes;
	}

	/**
	 * Utilise le bon lecteur en fonction de la ligne d'en-tête lu
	 * @param listeLigneFichier une ArrayList contenant toutes 
	 * les lignes à lire
	 * @return Une liste d'object, selon le lecteur utilisé.
	 * @throws WrongFileFormatException si l'en-tête du fichier 
	 * est incohérente
	 * @throws LectureException
	 * @throws IOException
	 */
	public static ArrayList<Object> lireFichier(ArrayList<String> listeLigneFichier) 
			throws WrongFileFormatException, LectureException, IOException {

		/**
		 * En-tête du csv des activitées
		 */
		final String EN_TETE_ACTIVITE = "Ident;Activité";

		/**
		 * En-tête du csv des employés
		 */
		final String EN_TETE_EMPLOYE = "Ident;Nom;Prenom;Telephone";

		/**
		 * En-tête du csv des réservations
		 */
		final String EN_TETE_RESERVATION = "Ident;salle;employe;activite;date;heuredebut;heurefin;;;;;";

		/**
		 * En-tête du csv des salles
		 */
		final String EN_TETE_SALLE = "Ident;Nom;Capacite;videoproj;ecranXXL;ordinateur;type;logiciels;imprimante";

		if (listeLigneFichier.isEmpty()) {
			throw new IOException("Le fichier est vide.");
		}

		//Fait l'appel en fonction de l'en-tête
		switch(listeLigneFichier.get(0)) {

		case EN_TETE_ACTIVITE :

			return lireActiviteCSV(listeLigneFichier);

		case EN_TETE_EMPLOYE :

			return lireEmployeCSV(listeLigneFichier);

		case EN_TETE_RESERVATION :

			return lireReservationCSV(listeLigneFichier);

		case EN_TETE_SALLE :

			return lireSalleCSV(listeLigneFichier);

		default :
			throw new LectureException();
		}
	}

	/**
	 * Créer une liste de salles, à partir des données de l'ArrayList
	 * Ajoute une description du profil dans le log
	 * @param listeLigneFichier une ArrayList 
	 * contenant toutes les lignes à lire
	 * @return Une liste de salles.
	 * @throws LectureException si données incohérentes
	 */
	public static ArrayList<Object> lireSalleCSV(ArrayList<String> listeLigneFichier) { //INITIALEMENT PRIVATE

		Salle salle;

		String id;
		int capacite;
		int nombrePc;

		boolean videoProjecteur;
		boolean ecranXxl;
		boolean imprimante;

		String nom;
		String typePc;

		String[] ligneSplit;

		ArrayList<String> logicielInstalle;
		ArrayList<Object> listeSalles = new ArrayList<Object>();

		listeLigneFichier.remove(0);

		for(String ligne : listeLigneFichier) {
			ligneSplit  = ligne.split(";");

			id = (ligneSplit.length > 0 && ligneSplit[0].matches("\\d+")) 
					? ligneSplit[0] : "Id inconnu";
			nom = (ligneSplit.length > 1 && ligneSplit[1].matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) ? ligneSplit[1] : "Nom inconnu";
			capacite = (ligneSplit.length > 2 && ligneSplit[2].matches("\\d+")) ? Integer.parseInt(ligneSplit[2]) : 0;
			videoProjecteur = (ligneSplit.length > 3) && (ligneSplit[3].equalsIgnoreCase("oui") ? true : false);
			ecranXxl = (ligneSplit.length > 4) && (ligneSplit[4].equalsIgnoreCase("oui") ? true : false);
			nombrePc = (ligneSplit.length > 5 && ligneSplit[5].matches("\\d+")) ? Integer.parseInt(ligneSplit[5]) : 0;
			typePc = (ligneSplit.length > 6 && ligneSplit[6].matches("^[A-Za-z ]+$")) ? ligneSplit[6] : "Type inconnu";

			logicielInstalle = (ligneSplit.length > 7) ?
					new ArrayList<>(Arrays.asList(ligneSplit[7].split(","))) : new ArrayList<>();

			imprimante = (ligneSplit.length > 8) && (ligneSplit[8].equalsIgnoreCase("oui") ? true : false);

			salle = new Salle(id, nom, capacite, videoProjecteur, ecranXxl, nombrePc, typePc, logicielInstalle, imprimante);
			listeSalles.add((Object) salle);
		}

		return listeSalles;
	}

	/**
	 * Créer une liste de réservations, 
	 * à partir des données de l'ArrayList
	 * Ajoute une description du profil dans le log
	 * @param listeLigneFichier une ArrayList 
	 * contenant toutes les lignes à lire
	 * @return Une liste de réservations.
	 * @throws LectureException si données incohérentes
	 */
	public static ArrayList<Object> lireReservationCSV(ArrayList<String> listeLigneFichier) 
			throws LectureException { //INITIALEMENT PRIVATE

		Reservation reservation;        

		String activite;
		String reservant;
		String salleReservee;

		String id;
		String date;
		String heureDebut;
		String heureFin;
		String objetReservation;
		String nomInterlocuteur;
		String prenomInterlocuteur;
		String usageSalle;

		int numeroInterlocuteur;

		String[] ligneSplit;

		ArrayList<Object> listeReservation = new ArrayList<Object>();

		if(listeLigneFichier.isEmpty()) {
			throw new LectureException();
		}
		
		listeLigneFichier.remove(0);

		// Cast des listes
		ArrayList<Employe> listeEmploye = RoomManager.stockage.getListeEmploye();
		ArrayList<Activite> listeActivite = RoomManager.stockage.getListeActivite();
		ArrayList<Salle> listeSalle = RoomManager.stockage.getListeSalle();
		ArrayList<String> listeIdEmploye = new ArrayList<>();
		ArrayList<String> listeNomActivite = new ArrayList<>();
		ArrayList<String> listeIdSalle = new ArrayList<>();

		for (Employe emp : listeEmploye) {
			listeIdEmploye.add(emp.getIdentifiant().trim());
		}

		for (Activite act : listeActivite) {
			String nom = act.getNom().trim();
			if(nom.equals("entretien de la salle")) {
				nom= "entretien";
			}
			listeNomActivite.add(nom);
		}

		for (Salle sal : listeSalle) {
			listeIdSalle.add(sal.getIdentifiant().trim());
		}

		for(String ligne : listeLigneFichier) {

			ligneSplit  = ligne.split(";");

			id = (ligneSplit.length > 0 && ligneSplit[0].length() > 1 && ligneSplit[0].charAt(0) == 'R') ? ligneSplit[0] : "Id inconnu";      
			salleReservee = (ligneSplit.length > 1 && ligneSplit[1].length() == 8 && ligneSplit[1].matches("\\d+")) ? ligneSplit[1] : "-1";
			reservant = (ligneSplit.length > 2 && ligneSplit[2].length() == 7 && ligneSplit[2].charAt(0) == 'E') ? ligneSplit[2] : "Employé inconnu";
			activite = (ligneSplit.length > 3 && ligneSplit[3].length() > 1) ? ligneSplit[3] : "Activite inconnue";

			if(!listeIdSalle.contains(salleReservee)) {
				throw new LectureException();
			}
			
			if(!listeIdEmploye.contains(reservant)) {
				throw new LectureException();
			}
			
			if(!listeNomActivite.contains(activite)) {
				throw new LectureException();
			} 

			date = (ligneSplit.length > 4 && ligneSplit[4].length() == 10 && ligneSplit[4].matches("\\d{2}/\\d{2}/\\d{4}")) ? ligneSplit[4] : "Date inconnu";
			heureDebut = (ligneSplit.length > 5 && ligneSplit[5].length() == 5 && ligneSplit[5].matches("\\d{2}h\\d{2}")) ? ligneSplit[5] : "Heure début inconnu";
			heureFin = (ligneSplit.length > 6 && ligneSplit[6].length() == 5 && ligneSplit[6].matches("\\d{2}h\\d{2}")) ? ligneSplit[6] : "Heure fin inconnu";
			objetReservation = (ligneSplit.length > 7 && ligneSplit[7].length() > 1) ? ligneSplit[7] : "Objet réservation inconnu";
			nomInterlocuteur = (ligneSplit.length > 8 && ligneSplit[8].length() > 1) ? ligneSplit[8] : "Nom inconnu";
			prenomInterlocuteur = (ligneSplit.length > 9 && ligneSplit[9].length() > 1) ? ligneSplit[9] : "Prenom inconnu";
			numeroInterlocuteur = (ligneSplit.length > 10 && ligneSplit[10].length() > 1 && ligneSplit[10].matches("\\d+")) ? Integer.parseInt(ligneSplit[10]) : 0;
			usageSalle = (ligneSplit.length > 11 && ligneSplit[11].length() > 1) ? ligneSplit[11] : "Usage inconnu";

			reservation = new Reservation(id, date, heureDebut, heureFin, objetReservation, nomInterlocuteur,
					prenomInterlocuteur, numeroInterlocuteur, usageSalle, reservant, activite, salleReservee);

			listeReservation.add((Object) reservation);
		}
		return listeReservation;
	}


	/**
	 * Créer une liste d'employés, à partir des données de l'ArrayList
	 * Ajoute une description du profil dans le log
	 * @param listeLigneFichier une ArrayList 
	 * contenant toutes les lignes à lire
	 * @return Une liste d'employéss.
	 * @throws LectureException si données incohérentes
	 */
	public static ArrayList<Object> lireEmployeCSV(ArrayList<String> listeLigneFichier) { //INITIALEMENT PRIVATE

		Employe employe;

		String id;
		String nom;
		String prenom;
		int tel;

		String[] ligneSplit;

		ArrayList<Object> listeEmploye = new ArrayList<Object>();

		listeLigneFichier.remove(0);

		for(String ligne : listeLigneFichier) {

			ligneSplit  = ligne.split(";");

			id = (ligneSplit.length > 0 && ligneSplit[0].length() == 7 && ligneSplit[0].charAt(0) == 'E') ? ligneSplit[0] : "Employe inconnu";
			nom = (ligneSplit.length > 1 && ligneSplit[1].length() > 1) ? ligneSplit[1] : "Nom inconnu";
			prenom = (ligneSplit.length > 2 && ligneSplit[2].length() > 1) ? ligneSplit[2] : "Prenom inconnu";
			tel = (ligneSplit.length > 3 && ligneSplit[3].length() == 4 && ligneSplit[3].matches("\\d+")) ? Integer.parseInt(ligneSplit[3]) : -1;

			employe = new Employe(id, nom, prenom, tel);
			listeEmploye.add((Object) employe);
		}

		return listeEmploye;
	}


	/**
	 * Créer une liste d'activitées, à partir des données de l'ArrayList
	 * Ajoute une description du profil dans le log
	 * @param listeLigneFichier une ArrayList 
	 * contenant toutes les lignes à lire
	 * @return Une liste d'activitées.
	 * @throws LectureException si données incohérentes
	 */
	public static ArrayList<Object> lireActiviteCSV(ArrayList<String> listeLigneFichier)  { //INITIALEMENT PRIVATE

		Activite activite;

		String id;
		String nom;

		String[] ligneSplit;

		ArrayList<Object> listeActivite = new ArrayList<Object>();

		listeLigneFichier.remove(0);

		for(String ligne : listeLigneFichier) {

			ligneSplit  = ligne.split(";");

			id = (ligneSplit.length > 0 && ligneSplit[0].length() == 8 && ligneSplit[0].charAt(0) == 'A') ? ligneSplit[0] : "Activite inconnu";
			nom = (ligneSplit.length > 1 && ligneSplit[1].length() > 1) ? ligneSplit[1] : "Nom inconnu";

			activite = new Activite(id, nom);
			listeActivite.add((Object) activite);
		}

		return listeActivite;
	}


	/**
	 * Obtenir l'extension d'un fichier
	 * @param filePath le chenmion du fichier à analyser
	 * @return l'extension du fichier
	 */

	public static String getExtensionFichier(String cheminFichier) {

		int indexPoint;

		indexPoint = cheminFichier.lastIndexOf('.');

		if (indexPoint > 0 && indexPoint < cheminFichier.length() - 1) {

			return cheminFichier.substring(indexPoint + 1);

		}
		return "";
	}
}