package newdrums;
//https://stackoverflow.com/questions/17847816/position-image-in-any-screen-resolution
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager2;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Drums {

    protected static BufferedImage SYMBOL;
    protected static BufferedImage DRUM;

    public static void main(String[] args) {
        new Drums();
    }

    public Drums() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        public TestPane() {
            setLayout(new PropertionalLayoutManager(400, 400));
            add(new Symbol(), new PropertionalConstraint(0f, 0));
            add(new Symbol(), new PropertionalConstraint(0.67f, 0));
            add(new Symbol(), new PropertionalConstraint(0f, 0.4675f));
            add(new Symbol(), new PropertionalConstraint(0.67f, 0.4675f));
            add(new Drum(), new PropertionalConstraint(0.205f, 0.1f));
            add(new Drum(), new PropertionalConstraint(0.5f, 0.1f));
            add(new Drum(), new PropertionalConstraint(0f, 0.33f));
            add(new Drum(), new PropertionalConstraint(0.705f, 0.33f));
        }
    }

    public class PropertionalConstraint {

        private float x;
        private float y;

        public PropertionalConstraint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public class PropertionalLayoutManager implements LayoutManager2 {

        private Map<Component, PropertionalConstraint> constraints;
        private Dimension defaultSize;

        public PropertionalLayoutManager(int defaultWidth, int defaultHeight) {
            constraints = new WeakHashMap<>(25);
            defaultSize = new Dimension(defaultWidth, defaultHeight);
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraint) {
            if (constraint instanceof PropertionalConstraint) {
                constraints.put(comp, ((PropertionalConstraint) constraint));
            }
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return preferredLayoutSize(target);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.5f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            constraints.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {

            return defaultSize;

        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {

            int width = parent.getWidth();
            int height = parent.getHeight();

            double dScaleWidth = getScaleFactor(defaultSize.width, width);
            double dScaleHeight = getScaleFactor(defaultSize.height, height);

            double scaleSize = Math.min(dScaleHeight, dScaleWidth);
            int minRange = Math.min(width, height);

            if (width > 0 && height > 0) {

                int maxY = 0;
                int maxX = 0;

                for (Component comp : parent.getComponents()) {
                    PropertionalConstraint p = constraints.get(comp);
                    if (p != null) {
                        Dimension prefSize = comp.getPreferredSize();

                        prefSize.width *= scaleSize;
                        prefSize.height *= scaleSize;

                        int x = Math.round(minRange * p.getX());
                        int y = Math.round(minRange * p.getY());

                        comp.setBounds(x, y, prefSize.width, prefSize.height);

                        maxX = Math.max(maxX, x + prefSize.width);
                        maxY = Math.max(maxY, y + prefSize.height);
                    } else {
                        comp.setBounds(0, 0, 0, 0);
                    }
                }

                for (Component comp : parent.getComponents()) {
                    System.out.println("maxX = " + maxX);
                    System.out.println("maxY = " + maxY);
                    if (comp.getWidth() > 0 && comp.getHeight() > 0) {
                        int x = ((width - maxX) / 2) + comp.getX();
                        int y = ((height - maxY) / 2) + comp.getY();
                        comp.setLocation(x, y);
                    }                    
                }

            } else {

                for (Component comp : parent.getComponents()) {
                    comp.setBounds(0, 0, 0, 0);
                }

            }

        }
    }

    public abstract class AbstractKitPiecePane extends JPanel {

        private BufferedImage scaled;

        public AbstractKitPiecePane() {
            setOpaque(false);
        }

        public abstract BufferedImage getKitImage();

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getKitImage().getWidth(), getKitImage().getHeight());
        }

        @Override
        public void invalidate() {
            super.invalidate();
            if (getWidth() > 0 && getHeight() > 0) {
                scaled = getScaledInstanceToFit(getKitImage(), getSize());
            } else {
                scaled = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (scaled != null) {
                int x = (getWidth() - scaled.getWidth()) / 2;
                int y = (getHeight() - scaled.getHeight()) / 2;
                g.drawImage(scaled, x, y, this);
            }
        }
    }

    public class Drum extends AbstractKitPiecePane {

        @Override
        public BufferedImage getKitImage() {
            return DRUM;
        }
    }

    public class Symbol extends AbstractKitPiecePane {

        @Override
        public BufferedImage getKitImage() {
            return SYMBOL;
        }
    }

    protected static BufferedImage getScaledInstance(BufferedImage img, double dScaleFactor) {

        BufferedImage imgScale = img;

        int iImageWidth = (int) Math.round(img.getWidth() * dScaleFactor);
        int iImageHeight = (int) Math.round(img.getHeight() * dScaleFactor);

        if (dScaleFactor <= 1.0d) {

            imgScale = getScaledDownInstance(img, iImageWidth, iImageHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        } else {

            imgScale = getScaledUpInstance(img, iImageWidth, iImageHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        }

        return imgScale;

    }

    protected static BufferedImage getScaledDownInstance(BufferedImage img,
            int targetWidth,
            int targetHeight,
            Object hint) {

//        System.out.println("Scale down...");
        int type = (img.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = (BufferedImage) img;

        if (targetHeight > 0 || targetWidth > 0) {
            int w, h;
            w = img.getWidth();
            h = img.getHeight();

            do {

                if (w > targetWidth) {
                    w /= 2;
                    if (w < targetWidth) {
                        w = targetWidth;
                    }
                }

                if (h > targetHeight) {
                    h /= 2;
                    if (h < targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(Math.max(w, 1), Math.max(h, 1), type);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(ret, 0, 0, w, h, null);
                g2.dispose();

                ret = tmp;

            } while (w != targetWidth || h != targetHeight);
        } else {
            ret = new BufferedImage(1, 1, type);
        }

        return ret;
    }

    protected static BufferedImage getScaledUpInstance(BufferedImage img,
            int targetWidth,
            int targetHeight,
            Object hint) {

        int type = BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = (BufferedImage) img;
        int w, h;
        w = img.getWidth();
        h = img.getHeight();

        do {
            if (w < targetWidth) {
                w *= 2;
                if (w > targetWidth) {
                    w = targetWidth;
                }
            }

            if (h < targetHeight) {
                h *= 2;
                if (h > targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
            tmp = null;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    public static BufferedImage getScaledInstanceToFit(BufferedImage img, Dimension size) {
        double scaleFactor = getScaleFactorToFit(img, size);
        return getScaledInstance(img, scaleFactor);
    }

    public static double getScaleFactorToFit(BufferedImage img, Dimension size) {
        double dScale = 1;

        if (img != null) {
            int imageWidth = img.getWidth();
            int imageHeight = img.getHeight();
            dScale = getScaleFactorToFit(new Dimension(imageWidth, imageHeight), size);
        }

        return dScale;
    }

    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;

        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }

        return dScale;
    }

    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        double dScale = 1;
        if (iMasterSize > iTargetSize) {
            dScale = (double) iTargetSize / (double) iMasterSize;
        } else {
            dScale = (double) iTargetSize / (double) iMasterSize;
        }

        return dScale;
    }

    static {

        try {
            SYMBOL = ImageIO.read(new File("Symbol.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            DRUM = ImageIO.read(new File("Drum.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}