
// Kartankatseluohjelman graafinen käyttöliittymä

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MapDialog extends JFrame {

	private String url = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=-180,-90,180,90&SRS=EPSG:4326&WIDTH=953&HEIGHT=480&LAYERS=bluemarble,cities&STYLES=&FORMAT=image/png&TRANSPARENT=true";

	// Käyttöliittymän komponentit

	private JLabel imageLabel = new JLabel();
	private JPanel leftPanel = new JPanel();

	private JButton refreshB = new JButton("Päivitä");
	private JButton leftB = new JButton("<");
	private JButton rightB = new JButton(">");
	private JButton upB = new JButton("^");
	private JButton downB = new JButton("v");
	private JButton zoomInB = new JButton("+");
	private JButton zoomOutB = new JButton("-");

	public MapDialog() throws Exception {

		// Valmistele ikkuna ja lisää siihen komponentit

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		
		//lataa oletusnäkymän
		imageLabel.setIcon(new ImageIcon(new URL(url)));

		add(imageLabel, BorderLayout.EAST);

		ButtonListener bl = new ButtonListener();
		refreshB.addActionListener(bl);
		leftB.addActionListener(bl);
		rightB.addActionListener(bl);
		upB.addActionListener(bl);
		downB.addActionListener(bl);
		zoomInB.addActionListener(bl);
		zoomOutB.addActionListener(bl);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		leftPanel.setMaximumSize(new Dimension(100, 600));

		// haetaan getcapabilities-kyselyn avulla halutut tiedot XML:stä

		URL url = new URL("http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(url.openStream());
		doc.getDocumentElement().normalize();

		NodeList layerlist = doc.getElementsByTagName("Layer");

		// lisätään kaikki layers-vaihtoehdot käyttöliittymään
		for (int i = 0; i < layerlist.getLength(); i++) {
			Node child1 = layerlist.item(i).getFirstChild().getNextSibling();
			Node sibling = child1.getNextSibling();
			String name = child1.getTextContent();
			String title = sibling.getNextSibling().getTextContent();
			leftPanel.add(new LayerCheckBox(name, title, true));
		}

		leftPanel.add(refreshB);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(leftB);
		leftPanel.add(rightB);
		leftPanel.add(upB);
		leftPanel.add(downB);
		leftPanel.add(zoomInB);
		leftPanel.add(zoomOutB);

		add(leftPanel, BorderLayout.WEST);

		pack();
		setVisible(true);

	}

	public static void main(String[] args) throws Exception {
		new MapDialog();
	}

	// Kontrollinappien kuuntelija
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == refreshB) {
				try {
					updateImage();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (e.getSource() == leftB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(-10, 0, -10, 0, 0), imageLabel);
				mapupdater.execute();
			}
			if (e.getSource() == rightB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(10, 0, 10, 0, 0), imageLabel);
				mapupdater.execute();
			}
			if (e.getSource() == upB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(0, 5, 0, 5, 0), imageLabel);
				mapupdater.execute();
			}
			if (e.getSource() == downB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(0, -5, 0, -5, 0), imageLabel);
				mapupdater.execute();
			}
			if (e.getSource() == zoomInB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(0, 0, 0, 0, 1), imageLabel);
				mapupdater.execute();
			}
			if (e.getSource() == zoomOutB) {
				//haetaan uusi kuva erillisessä säikeessä
				MapUpdater mapupdater = new MapUpdater(moveOnTheMap(0, 0, 0, 0, -1), imageLabel);
				mapupdater.execute();
			}
		}
	}

	// Valintalaatikko, joka muistaa karttakerroksen nimen
	private class LayerCheckBox extends JCheckBox {
		private String name = "";

		public LayerCheckBox(String name, String title, boolean selected) {
			super(title, null, selected);
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	// Tarkastetaan mitkä karttakerrokset on valittu,
	// tehdään uudesta karttakuvasta pyyntö palvelimelle ja päivitetään kuva
	public void updateImage() throws Exception {
		String s = "";

		// Tutkitaan, mitkä valintalaatikot on valittu, ja
		// kerätään s:ään pilkulla erotettu lista valittujen kerrosten
		// nimistä (käytetään haettaessa uutta kuvaa)
		Component[] components = leftPanel.getComponents();
		for (Component com : components) {
			if (com instanceof LayerCheckBox)
				if (((LayerCheckBox) com).isSelected())
					s = s + com.getName() + ",";
		}
		if (s.endsWith(","))
			s = s.substring(0, s.length() - 1);

		// muodostetaan url splittaamalla alkuperäinen url '&' merkkien kohdalta ja
		// lisäämällä halutut parametrit tilalle
		String[] split = getURL().split("&");
		split[7] = "LAYERS=" + s;
		System.out.println(split[7]);

		String newURL = "";
		for (String a : split) {
			newURL += a + "&";
		}
		newURL = newURL.substring(0, newURL.length() - 1);
		//tallennetaan uusi url nykyisen urlin tilalle
		setURL(newURL);
		//haetaan uusi karttakuva eri säikeessä SwingWorkeriä hyväksikäyttäen
		MapUpdater mapupdater = new MapUpdater(newURL, imageLabel);
		mapupdater.execute();
	}

	public String moveOnTheMap(int x1, int y1, int x2, int y2, int zoom) {
		String newURL = "";
		//tarkistetaan, että pysytään haluttujen rajojen sisällä kartalla liikkuessa
		if (zoom == 0 && getCoordinates(0) + x1 >= -180 && getCoordinates(1) + y1 >= -90
				&& getCoordinates(2) + x2 <= 180 && getCoordinates(3) + y2 < 90) {
			String[] split = getURL().split("&");
			split[3] = "BBOX=" + (getCoordinates(0) + x1) + "," + (getCoordinates(1) + y1) + ","
					+ (getCoordinates(2) + x2) + "," + (getCoordinates(3) + y2);
			newURL = "";
			
			//kootaan url uudestaan sen pilkkomisen ja muuttamisen jälkeen
			for (String a : split) {
				newURL += a + "&";
			}
			
			//poistetaan viimeinen &-merkki
			newURL = newURL.substring(0, newURL.length() - 1);
			setURL(newURL);
		}
		// tarkistetaan että zoomatessa pysytään kartan äärirajojen sisällä, jonka
		// jälkeen muutetaan koordinaatteja ja palautetaan uusi url
		else if ((zoom == 1 || zoom == -1) && (getCoordinates(0) + (zoom * 10)) < (getCoordinates(2) + (-zoom * 10))
				&& (getCoordinates(1) + (zoom * 5)) < (getCoordinates(3) + (-zoom * 5))
				&& (getCoordinates(0) + (zoom * 10) >= -180 && getCoordinates(1) + (zoom * 5) >= -90
				&& getCoordinates(2) + (-zoom * 10) <= 180 && getCoordinates(3) + (-zoom * 5) < 90)) {
			
			String[] split = getURL().split("&");
			split[3] = "BBOX=" + (getCoordinates(0) + (zoom * 10)) + "," + (getCoordinates(1) + (zoom * 5)) + ","
					+ (getCoordinates(2) + (-zoom * 10)) + "," + (getCoordinates(3) + (-zoom * 5));
			
			System.out.println(split[3]);
			newURL = "";
			//kootaan url uudestaan sen pilkkomisen ja muuttamisen jälkeen
			for (String a : split) {
				newURL += a + "&";
			}
			//poistetaan viimeinen &-merkki
			newURL = newURL.substring(0, newURL.length() - 1);
			setURL(newURL);

		} else {
			newURL = getURL();
		}
		return newURL;
	}
	
	//metodi nykyisten koordinaattien hakemiseen nykyisesta urlista
	public int getCoordinates(int x) {
		String[] split = getURL().split("&");
		String coordinates = split[3];
		coordinates = coordinates.substring(5, coordinates.length());
		String[] splitCoordinates = coordinates.split(",");
		if (x >= 0 && x < splitCoordinates.length) {
			return Integer.parseInt(splitCoordinates[x]);
		} else {
			return 0;
		}

	}

	//setteri ja getteri url-muuttujalle
	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}
} // MapDialog
