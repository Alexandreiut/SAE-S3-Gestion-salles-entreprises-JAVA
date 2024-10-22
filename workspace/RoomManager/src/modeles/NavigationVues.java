package modeles;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Classe permettant la navigation entre les vues de l'application.
 */ 

public class NavigationVues {	
	/**
	 * Chemin vers le dossier racine des vues à partir du 
	 * contrôleur courant.
	 */
	private static final String RACINE_VUES = "/affichages/";

	/**
	 * Scène courante définie.
	 * 
	 * La scène par défaut est celle définie directement 
	 * par le Main.java au lancement de l'application : 
	 * le menu principal.
	 */
	private static Scene sceneCourante;
	
	/**
	 * Le chemin de la vue courante.
	 */
	private static String vueCourante;
	
	
	/**
	 * (Re)définie directement l'objet de la scène
	 * courante. Cette information est utile lors du 
	 * changement de scène via la méthode de changement
	 * de vue.
	 * @param nouvelleScene scène à afficher
	 */
	public static void setSceneCourante(Scene nouvelleScene) {
		sceneCourante = nouvelleScene;
	}
	
	
	/**
	 * Change de vue vers celle envoyée en 
	 * paramètre.
	 * @param routeVueFXML Nom du fichier FXML de la vue cible
	 */
	public static void changerVue(String routeVueFXML) {
		if (sceneCourante == null) {
			System.out.println("Erreur : aucune scène courante !");
		} else {
			try {
				Parent racine;
				racine = FXMLLoader.load(NavigationVues.class
						              .getResource(RACINE_VUES + routeVueFXML + ".fxml"));
				
				sceneCourante.setRoot(racine);
				vueCourante = routeVueFXML;
			} catch (IOException e) {
				System.out.println("ERREUR CHARGEMENT VUE :\n" + e.getMessage());
			}
		}
	}
	
	
	/** @return La scène courante. */
	public static Scene getScene() {
		return sceneCourante;
	}
	
	
	/** @return La vue courante. */
	public static String getVueCourante() {
		return vueCourante;
	}
}

