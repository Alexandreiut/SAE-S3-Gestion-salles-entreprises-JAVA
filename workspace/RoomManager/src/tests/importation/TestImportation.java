package tests.importation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import lanceur.RoomManager;
import modeles.erreur.LectureException;
import modeles.importation.Importation;
import modeles.items.Activite;
import modeles.items.Employe;
import modeles.items.Reservation;
import modeles.items.Salle;
import modeles.stockage.Stockage;

class TestImportation {

	@Test
	void testEstIPValide() {
				
		assertTrue(Importation.estIPValide("125.45.12.0"));
		assertTrue(Importation.estIPValide("192.68.101.13"));
		
		assertFalse(Importation.estIPValide("1000.0.0.0"));
		assertFalse(Importation.estIPValide("101.0.0"));
	}
	
	@Test
	void testTraiterImportFichiers() {
		
		RoomManager.stockage = new Stockage(new ArrayList<Salle>(),
							                new ArrayList<Activite>(),
							                new ArrayList<Employe>(),
							                new ArrayList<Reservation>());
		
		List<File> listeFichiersTest;
		
		listeFichiersTest = new ArrayList<>();
		
		listeFichiersTest.add(new File("src/ressource/csv/activites 26_08_24 13_40.csv"));
		
		assertEquals(listeFichiersTest, assertDoesNotThrow(
				     () -> Importation.traiterImportFichiers(listeFichiersTest)));
		
	}
	
	@Test
	void testGetNomsFichiersManquants() {
		
		RoomManager.stockage = new Stockage(new ArrayList<Salle>(),
							                new ArrayList<Activite>(),
							                new ArrayList<Employe>(),
							                new ArrayList<Reservation>());
		
		List<File> listeFichiersTest;
		
		listeFichiersTest = new ArrayList<>();
		
		listeFichiersTest.add(new File("src/ressource/csv/activites 26_08_24 13_40.csv"));
		
		List<String> nomsFichiersAttendus = new ArrayList<>();
		nomsFichiersAttendus.add("Employé");
		nomsFichiersAttendus.add("Salle");
		
		assertEquals(nomsFichiersAttendus, Importation.getNomsFichiersManquants(listeFichiersTest));
	}
	
	@Test
	void testImporterObjets() {
		
		ArrayList<Object> listeObjets = new ArrayList<>();
		ArrayList<String> fichiersDejaImportes = new ArrayList<>();
		
		ArrayList<String> attendu = new ArrayList<>();
		
		Stockage stockage = new Stockage(new ArrayList<Salle>(),
						                 new ArrayList<Activite>(),
						                 new ArrayList<Employe>(),
						                 new ArrayList<Reservation>());
		
		listeObjets.add(new Activite("A0000001", "réunion"));
		
		attendu.add("Activité");
		
		assertEquals(attendu, Importation.importerObjets(listeObjets, "Activité",
				                                         fichiersDejaImportes, stockage));
		
		listeObjets.clear();
		listeObjets.add(new Employe("E000001", "Dupont", "Pierre", 2614));
		
		attendu.clear();
		attendu.add("Employé");
		
		fichiersDejaImportes.add("Activité");
		
		assertEquals(attendu, Importation.importerObjets(listeObjets, "Employé",
				                                         fichiersDejaImportes, stockage));
		
		fichiersDejaImportes.clear();
		attendu.clear();
		
		assertEquals(attendu, Importation.importerObjets(listeObjets, "Employé",
                										 fichiersDejaImportes, stockage));
	}
}
