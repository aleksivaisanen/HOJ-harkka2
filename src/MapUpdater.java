import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/**
 * 
 * MapUpdater-luokka, jossa kartan p�ivitys tapahtuu
 * Kartan p�ivitys tapahtuu SwingWorkerin avulla, jottai k�ytt�liittym� ei tukkiudu uutta kuvaa odotellessa
 *
 */

public class MapUpdater extends SwingWorker<Integer, Void> {
	private String url;
	private JLabel label;
	
	public MapUpdater(String url, JLabel label) {
		this.url = url;
		this.label = label;
	}
	
	protected Integer doInBackground(){
		try {
			label.setIcon(new ImageIcon(new URL(url)));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return 1;
	}
}
