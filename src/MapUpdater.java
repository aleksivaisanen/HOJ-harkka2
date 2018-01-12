import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class MapUpdater implements Runnable {
	private String url;
	private JLabel label;
	
	public MapUpdater(String url, JLabel label) {
		this.url = url;
		this.label = label;
	}
	
	public void run(){
		try {
		label.setIcon(new ImageIcon(new URL(url)));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	//public JLabel getMapImg() {
		//return imageLabel.setIcon(new ImageIcon(url));	
	//}
}
