package angrypixel.gallery;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 739436930393162976L;

	private BufferedImage img;

	public ImagePanel(BufferedImage img) {
		this.img = img;
	}

	public void setImage(BufferedImage image) {
		this.img = image;
		this.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(this.getBackground());
		g.clearRect(0, 0, this.getWidth(), this.getHeight());

		if(img != null) {
			int maxDim = Math.max(img.getWidth(), img.getHeight());
			float relWidth = (1.0F - (maxDim - img.getWidth()) / (float) maxDim);
			float relHeight = (1.0F - (maxDim - img.getHeight()) / (float) maxDim);
			int drawnWidth = (int) Math.floor(this.getWidth() * relWidth);
			int drawnHeight = (int) Math.floor(this.getHeight() * relHeight);
			g.drawImage(img.getScaledInstance(drawnWidth, drawnHeight, Image.SCALE_FAST), this.getWidth() / 2 - drawnWidth / 2, this.getHeight() / 2 - drawnHeight / 2, null);
		}
	}

}