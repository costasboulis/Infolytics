package com.cleargist.catalog.deals.scrape;

import java.util.List;
import java.util.Locale;

import com.cleargist.catalog.deals.entity.jaxb.AddressListType;
import com.cleargist.catalog.deals.entity.jaxb.AddressType;

/**
 * Gets as input textLowercase and outputs an AddressType object. Works only for Greek addresses and care should be taken 
 * so that the input textLowercase contains only address information
 * 
 * @author kboulis
 *
 */
public class GreekAddressResolver {
	private static final Locale locale = new Locale("el", "GR"); 
	
	public static AddressListType resolve(String text) {
		String textLowercase = text.toLowerCase(locale).replaceAll(",", "");
		AddressListType addrList = new AddressListType();
		List<AddressType> addressList = addrList.getAddress() ;
		if (textLowercase.contains("πειραιάς")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Pireas");
			
			addressList.add(address);
		}
		if (textLowercase.contains("κορυδαλλός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Koridallos");

			addressList.add(address);
		}
		if (textLowercase.contains("νέο φάληρο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Neo Faliro");

			addressList.add(address);
		}
		if (textLowercase.contains("δραπετσώνα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Drapetsona");

			addressList.add(address);
		}
		if (textLowercase.contains("ρέντης")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Rentis");

			addressList.add(address);
		}
		if (textLowercase.contains("μοσχάτο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Moschato");

			addressList.add(address);
		}
		if (textLowercase.contains("νίκαια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Nikaia");

			addressList.add(address);
		}
		if (textLowercase.contains("πέραμα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Piraeus and neighboring suburbs");
			address.setCity("Perama");

			addressList.add(address);
		}
		if (textLowercase.contains("σύνταγμα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("ομόνοια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("κολωνάκι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("ακροπολη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("πλάκα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("νέος κόσμος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("παγκράτι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("πετράλωνα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("κουκάκι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("γκάζι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("ψυρρή")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("αμπελόκηποι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("πατήσια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("σεπόλια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("κυψέλη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("κολωνός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("μεταξουργείο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("πλατεία μαβίλη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("κολωνός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("γαλάτσι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("γκύζη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("ιλίσια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens Center");
			address.setCity("Athina");

			addressList.add(address);
		}
		if (textLowercase.contains("αθήνα ") || textLowercase.contains("κέντρο αθήνας")) {
			if (addressList.size() == 1) {
				AddressType address = addressList.get(0);
				if (!address.getCity().equals("Athina")) {
					AddressType address2 = new AddressType();
					address2.setGeographicalArea("Athens Center");
					address2.setCity("Athina");

					addressList.add(address2);
				}
			}
			else if (addressList.size() == 0) {
				AddressType address2 = new AddressType();
				address2.setGeographicalArea("Athens Center");
				address2.setCity("Athina");

				addressList.add(address2);
			}
			
		}
		if (textLowercase.contains("ζωγράφου")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Zografou");

			addressList.add(address);
		}
		if (textLowercase.contains("καισαριανή")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Kaisariani");

			addressList.add(address);
		}
		if (textLowercase.contains("καλλιθέα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Kallithea");

			addressList.add(address);
		}
		if (textLowercase.contains("νέα σμύρνη") || textLowercase.contains("ν. σμύρνη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Nea Smirni");

			addressList.add(address);
		}
		if (textLowercase.contains("γλυφάδα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Glifada");

			addressList.add(address);
		}
		if (textLowercase.contains(" βούλα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Voula");

			addressList.add(address);
		}
		if (textLowercase.contains("βουλιαγμένη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Vouliagmeni");

			addressList.add(address);
		}
		if (textLowercase.contains(" βάρη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Vari");

			addressList.add(address);
		}
		if (textLowercase.contains("ηλιούπολη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Ilioupoli");

			addressList.add(address);
		}
		if (textLowercase.contains("αργυρούπολη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Argiroupoli");

			addressList.add(address);
		}
		if (textLowercase.contains(" δάφνη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Dafni");
			
			addressList.add(address);
		}
		if (textLowercase.contains("υμηττός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Imitos");

			addressList.add(address);
		}
		if (textLowercase.contains("άλιμος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Alimos");

			addressList.add(address);
		}
		if (textLowercase.contains("ελληνικό ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Elliniko");

			addressList.add(address);
		}
		if (textLowercase.contains("άγιος δημήτριος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Agios Dimitrios");

			addressList.add(address);
		}
		if ((textLowercase.contains("παλαιό φάληρο")) || textLowercase.contains("π. φάληρο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens South and Center Suburbs");
			address.setCity("Palaio Faliro");

			addressList.add(address);
		}
		if (textLowercase.contains("κηφισιά")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Kifisia");

			addressList.add(address);
		}
		if (textLowercase.contains("μαρούσι") || textLowercase.contains("mαρούσι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Marousi");

			addressList.add(address);
		}
		if (textLowercase.contains("χαλάνδρι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Halandri");

			addressList.add(address);
		}
		if (textLowercase.contains("φιλοθέη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Filothei");

			addressList.add(address);
		}
		if (textLowercase.contains("ψυχικό")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Psihiko");

			addressList.add(address);
		}
		if (textLowercase.contains("αγία παρασκευή")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Agia Paraskevi");

			addressList.add(address);
		}
		if (textLowercase.contains("πεύκη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Pefki");

			addressList.add(address);
		}
		if (textLowercase.contains("νέα ερυθραία")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Nea Erithrea");

			addressList.add(address);
		}
		if (textLowercase.contains(" εκάλη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Ekali");

			addressList.add(address);
		}
		if (textLowercase.contains("βριλήσια") || textLowercase.contains("βριλήσσια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Vrilissia");

			addressList.add(address);
		}
		if (textLowercase.contains("μελήσια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Melisia");

			addressList.add(address);
		}
		if (textLowercase.contains("λυκόβρυση ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Likovrisi");

			addressList.add(address);
		}
		if (textLowercase.contains("θρακομακεδόνες ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Thrakomakedones");

			addressList.add(address);
		}
		if (textLowercase.contains("άνοιξη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Anoixi");

			addressList.add(address);
		}
		if (textLowercase.contains("πεντέλη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Penteli");

			addressList.add(address);
		}
		if (textLowercase.contains("διόνυσος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Dionisos");

			addressList.add(address);
		}
		if (textLowercase.contains("άγιος στέφανος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Agios Stefanos");

			addressList.add(address);
		}
		if (textLowercase.contains("δροσιά ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Drosia");

			addressList.add(address);
		}
		if (textLowercase.contains("μεταμόρφωση")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Metamorfosi");

			addressList.add(address);
		}
		if (textLowercase.contains("κρυονέρι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens North Suburbs");
			address.setCity("Krioneri");

			addressList.add(address);
		}
		if (textLowercase.contains("περιστέρι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Peristeri");

			addressList.add(address);
		}
		if (textLowercase.contains("αιγάλεω")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Aigaleo");

			addressList.add(address);
		}
		if (textLowercase.contains("χαϊδάρι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Haidari");

			addressList.add(address);
		}
		if (textLowercase.contains("αγία βαρβάρα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Agia Varvara");

			addressList.add(address);
		}
		if (textLowercase.contains("ίλιον")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Ilion");

			addressList.add(address);
		}
		if (textLowercase.contains("πετρούπολη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Petroupoli");

			addressList.add(address);
		}
		if (textLowercase.contains("καματερό")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("kamatero");

			addressList.add(address);
		}
		if (textLowercase.contains("μενίδι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("menidi");

			addressList.add(address);
		}
		if (textLowercase.contains("φιλαδέλφεια ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Nea Filadelphia");

			addressList.add(address);
		}
		if (textLowercase.contains(" ιωνία ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Nea Ionia");

			addressList.add(address);
		}
		if (textLowercase.contains("νέο ηράκλειο ") || textLowercase.contains("ν. ηράκλειο ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Neo Irakleio");

			addressList.add(address);
		}
		if (textLowercase.contains("χαλκηδόνα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Nea Halkidona");

			addressList.add(address);
		}
		if (textLowercase.contains("άγιοι ανάργυροι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Agioi Anargiroi");

			addressList.add(address);
		}
		if (textLowercase.contains("χασιά ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Athens West Suburbs");
			address.setCity("Hasia");

			addressList.add(address);
		}
		if (textLowercase.contains("ασπρόπυργος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Aspropirgos");

			addressList.add(address);
		}
		if (textLowercase.contains("μέγαρα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Megara");

			addressList.add(address);
		}
		if (textLowercase.contains("μαγούλα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Magoula");

			addressList.add(address);
		}
		if (textLowercase.contains("ελευσίνα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Elefsina");

			addressList.add(address);
		}
		if (textLowercase.contains("σαλαμίνα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Salamina");

			addressList.add(address);
		}
		if (textLowercase.contains("σούνιο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Lavrio");

			addressList.add(address);
		}
		if (textLowercase.contains("λαύριο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Lavrio");

			addressList.add(address);
		}
		if (textLowercase.contains("σαρωνίδα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Saronida");

			addressList.add(address);
		}
		if (textLowercase.contains("ανάβυσσος") || textLowercase.contains("παραλία ανάβυσσου")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Anavisos");

			addressList.add(address);
		}
		if (textLowercase.contains("λαγονήσι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Lagonisi");

			addressList.add(address);
		}
		if (textLowercase.contains("φώκαια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Anavisos");

			addressList.add(address);
		}
		if (textLowercase.contains("ραφήνα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Rafina");

			addressList.add(address);
		}
		if (textLowercase.contains("αρτέμιδα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Artemida");

			addressList.add(address);
		}
		if (textLowercase.contains("νέα μάκρη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Nea Makri");

			addressList.add(address);
		}
		if (textLowercase.contains("ζούμπερι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Zouberi");

			addressList.add(address);
		}
		if (textLowercase.contains("ωρωπός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Oropos");

			addressList.add(address);
		}
		if (textLowercase.contains("καπανδρίτι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Kapandriti");

			addressList.add(address);
		}
		if (textLowercase.contains("μαραθώνας")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Marathonas");

			addressList.add(address);
		}
		if (textLowercase.contains("πολυδένδρι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Kapandriti");

			addressList.add(address);
		}
		if (textLowercase.contains("αφίδνες")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Kapandriti");

			addressList.add(address);
		}
		if (textLowercase.contains("μαρκόπουλο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Markopoulo");

			addressList.add(address);
		}
		if (textLowercase.contains("μαλακάσα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Marathonas");

			addressList.add(address);
		}
		if (textLowercase.contains("γέρακας")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Gerakas");
			
			addressList.add(address);
		}
		if (textLowercase.contains("γλυκά νερά")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Glyka Nera");

			addressList.add(address);
		}
		if (textLowercase.contains("παιανία")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Paiania");

			addressList.add(address);
		}
		if (textLowercase.contains("κορωπί")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Koropi");

			addressList.add(address);
		}
		if (textLowercase.contains("πικέρμι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Pikermi");

			addressList.add(address);
		}
		if (textLowercase.contains("σπάτα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Attica");
			address.setCity("Spata");

			addressList.add(address);
		}
		if (textLowercase.contains("θεσσαλονίκη ") || textLowercase.contains("κέντρο θεσσαλονίκης")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Thessaloniki");
			address.setCity("Thessaloniki");

			addressList.add(address);
		}
		else {
			if (textLowercase.contains("καλαμαριά ") || textLowercase.contains("ντεπώ") || textLowercase.contains("χαριλάου ")) {
				AddressType address = new AddressType();
				address.setGeographicalArea("Thessaloniki");
				address.setCity("Thessaloniki");

				addressList.add(address);
			}
		}
		if (textLowercase.contains("αίγινα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Aigina");

			addressList.add(address);
		}
		if (textLowercase.contains("σπέτσες")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Spetses");

			addressList.add(address);
		}
		if (textLowercase.contains("μύκονος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Mykonos");

			addressList.add(address);
		}
		if (textLowercase.contains("σαντορίνη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Santorini");

			addressList.add(address);
		}
		if (textLowercase.contains("νάξος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Naxos");

			addressList.add(address);
		}
		if (textLowercase.contains("πάρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Paros");

			addressList.add(address);
		}
		if (textLowercase.contains(" ίος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Ios");

			addressList.add(address);
		}
		if (textLowercase.contains(" σύρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Siros");

			addressList.add(address);
		}
		if (textLowercase.contains(" άνδρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Andros");

			addressList.add(address);
		}
		if (textLowercase.contains("σίφνος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Sifnos");

			addressList.add(address);
		}
		if (textLowercase.contains(" μήλος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Milos");

			addressList.add(address);
		}
		if (textLowercase.contains("κύθνος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Kithnos");

			addressList.add(address);
		}
		if (textLowercase.contains("φολέγανδρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Folegandros");

			addressList.add(address);
		}
		if (textLowercase.contains("αμοργός")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Cyclades");
			address.setCity("Amorgos");

			addressList.add(address);
		}
		if (textLowercase.contains("ρόδος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Rodos");

			addressList.add(address);
		}
		if (textLowercase.contains("κάρπαθος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Karpathos");

			addressList.add(address);
		}
		if (textLowercase.contains(" κως")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Kos");

			addressList.add(address);
		}
		if (textLowercase.contains(" τήλος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Tilos");

			addressList.add(address);
		}
		if (textLowercase.contains("αστυπάλαια")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Astipalaia");

			addressList.add(address);
		}
		if (textLowercase.contains("κάλυμνος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Kalimnos");

			addressList.add(address);
		}
		if (textLowercase.contains("πάτμος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Patmos");

			addressList.add(address);
		}
		if (textLowercase.contains("λέρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Leros");

			addressList.add(address);
		}
		if (textLowercase.contains("νίσυρος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Nisiros");

			addressList.add(address);
		}
		if (textLowercase.contains(" σύμη")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Dodekanissa");
			address.setCity("Simi");

			addressList.add(address);
		}
		if (textLowercase.contains("κέρκυρα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Ionian islands");
			address.setCity("Kerkira");

			addressList.add(address);
		}
		if (textLowercase.contains("ζάκυνθος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Ionian islands");
			address.setCity("Zakinthos");

			addressList.add(address);
		}
		if (textLowercase.contains("κεφαλονιά") || textLowercase.contains("κεφαλλονιά")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Ionian islands");
			address.setCity("Kefalonia");

			addressList.add(address);
		}
		if (textLowercase.contains("ιθάκη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Ionian islands");
			address.setCity("Ithaki");

			addressList.add(address);
		}
		if (textLowercase.contains("λευκάδα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Ionian islands");
			address.setCity("Lefkada");

			addressList.add(address);
		}
		if (textLowercase.contains("λέσβος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Lesvos");

			addressList.add(address);
		}
		if (textLowercase.contains(" χίος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Chios");

			addressList.add(address);
		}
		if (textLowercase.contains("αράχωβα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Arachova");

			addressList.add(address);
		}
		if (textLowercase.contains("παρνασσ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Parnassos");

			addressList.add(address);
		}
		if (textLowercase.contains("μέτσοβο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Metsovo");

			addressList.add(address);
		}
		if (textLowercase.contains("ζαγόρ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Zagoroxoria");

			addressList.add(address);
		}
		if (textLowercase.contains("τρίκαλα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Trikala");

			addressList.add(address);
		}
		if (textLowercase.contains(" μάνη ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Mani");

			addressList.add(address);
		}
		if (textLowercase.contains("ναύπλιο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Nafplio");

			addressList.add(address);
		}
		if (textLowercase.contains("καρπενήσι")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Karpenisi");

			addressList.add(address);
		}
		if (textLowercase.contains(" πήλιο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Pilio");

			addressList.add(address);
		}
		if (textLowercase.contains("εύβοια ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Evia");

			addressList.add(address);
		}
		if (textLowercase.contains("χαλκίδα ")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Rest of Greece");
			address.setCity("Halkida");

			addressList.add(address);
		}
		if (textLowercase.contains("χανιά")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Crete");
			address.setCity("Chania");

			addressList.add(address);
		}
		if (textLowercase.contains("ρέθυμνο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Crete");
			address.setCity("Rethimno");

			addressList.add(address);
		}
		if (textLowercase.contains("ηράκλειο")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Crete");
			address.setCity("Irakleio");

			addressList.add(address);
		}
		if (textLowercase.contains("γιος νικόλαος")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Crete");
			address.setCity("Agios Nikolaos");

			addressList.add(address);
		}
		if (textLowercase.contains("ελούντα")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Crete");
			address.setCity("Elouda");

			addressList.add(address);
		}
		if (textLowercase.contains("χαλκιδική")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Halkidiki");
			address.setCity("Halkidiki");

			addressList.add(address);
		}
		if (textLowercase.contains("βουλγαρία") || textLowercase.contains("ελβετία") || textLowercase.contains("αυστρία") ||
				textLowercase.contains("γαλλία") || textLowercase.contains("ιταλία") || textLowercase.contains("αγγλία") || 
				textLowercase.contains("τουρκία") || textLowercase.contains("νορβηγία")) {
			AddressType address = new AddressType();
			address.setGeographicalArea("Foreign destinations");
			address.setCity("Unknown");

			addressList.add(address);
		}
		
		if (addressList.size() == 0) {
			AddressType address = new AddressType();
			address.setCity(textLowercase);
			address.setGeographicalArea("Unknown");
			
			addressList.add(address);
		}
		
		
		return addrList;
	}
}
