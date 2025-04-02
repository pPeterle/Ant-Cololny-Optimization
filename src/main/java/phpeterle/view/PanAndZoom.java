package phpeterle.view;

import phpeterle.modelos.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.List;

public class PanAndZoom {
    
    PanAndZoomCanvas canvas;
    AffineTransform at;   // the current pan and zoom transform
    Point2D XFormedPoint; // storage for a transformed mouse point
    
    List<Formiga> caminhoes;
    List<Localidade> localidades;
    List<Localidade> hoteis;

    public String[] mColors = {
            "#39add1", // light blue
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#637a91", // dark gray
            "#f092b0", // pink
            "#b7c0c7"  // light gray
    };
    
    public PanAndZoom(List<Formiga> caminhoes, List<Localidade> localidades, List<Localidade> hoteis) {
        this.caminhoes = caminhoes;
        this.localidades = localidades;
        this.hoteis = hoteis;
        JFrame frame = new JFrame();
        canvas = new PanAndZoomCanvas();
        PanningHandler panner = new PanningHandler();
        canvas.addMouseListener(panner);
        canvas.addMouseMotionListener(panner);
        canvas.setBorder(BorderFactory.createLineBorder(Color.black));
        
        // code for handling zooming
        JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 500, 100);
        zoomSlider.setMajorTickSpacing(25);
        zoomSlider.setMinorTickSpacing(5);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);
        zoomSlider.addChangeListener(new ScaleHandler());
        
        // Add the components to the canvas
        frame.getContentPane().add(zoomSlider, BorderLayout.NORTH);
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    class PanAndZoomCanvas extends JComponent {
        double translateX;
        double translateY;
        double scale;
        
        PanAndZoomCanvas() {
            translateX =  4463.328092993274;
            translateY = 9245.847651467619;
            scale = 1;
        }
        
        public void paintComponent(Graphics g) {
            Graphics2D ourGraphics = (Graphics2D) g;
            // save the original transform so that we can restore
            // it later
            AffineTransform saveTransform = ourGraphics.getTransform();
            
            // blank the screen. If we do not call super.paintComponent, then
            // we need to blank it ourselves
            ourGraphics.setColor(Color.WHITE);
            ourGraphics.fillRect(0, 0, getWidth(), getHeight());
            
            // We need to add new transforms to the existing
            // transform, rather than creating a new transform from scratch.
            // If we create a transform from scratch, we will
            // will start from the upper left of a JFrame,
            // rather than from the upper left of our component
            at = new AffineTransform(saveTransform);
            
            // The zooming transformation. Notice that it will be performed
            // after the panning transformation, zooming the panned scene,
            // rather than the original scene
            at.translate(getWidth() / 2, getHeight() / 2);
            at.scale(scale, scale);
            at.translate(-getWidth() / 2, -getHeight() / 2);
            
            // The panning transformation
            at.translate(translateX, translateY);
            
            ourGraphics.setTransform(at);
            
            // draw the objects
            ourGraphics.setColor(Color.BLACK);
            ourGraphics.setFont(new Font("TimesRoman", Font.PLAIN, 10));
            
            for (int i = 0; i < caminhoes.size(); i ++) {
                Formiga formiga = caminhoes.get(i);
                Polygon polygon = new Polygon();
                for (int j = 0; j < formiga.cidadesVisitadas.size(); j++) {
                    Localidade localidade = formiga.cidadesVisitadas.get(j);
                    polygon.addPoint((int) (localidade.getX() * 200), (int) (localidade.getY() * 200));
                    
                }
                
                
                ourGraphics.setColor(Color.decode(mColors[i]));
                ourGraphics.fill(polygon);
                
                ourGraphics.setColor(Color.BLACK);
                
                for (int j = 1; j < formiga.cidadesVisitadas.size(); j++) {
                    Localidade l1 = formiga.cidadesVisitadas.get(j -1);
                    Localidade l2 = formiga.cidadesVisitadas.get(j);
                    
                    drawArrowLine(ourGraphics, (int) (l1.getX() * 200), (int) (l1.getY() * 200), (int) (l2.getX() * 200), (int) (l2.getY() * 200), 1, 1);
                    
                }
                
            }
            
            for (Localidade localidade : localidades.stream().filter(localidade -> !localidade.recebeuEntrega()).toList()) {
                ourGraphics.fillOval((int) (localidade.getX() * 200),(int)  (localidade.getY() * 200), 10, 10);
                ourGraphics.drawString(localidade.getNome(), (int) (localidade.getX() * 200), (int) (localidade.getY() * 200));
            }

//            for (Localidade localidade : hoteis) {
//                ourGraphics.fillOval((int) (localidade.getX() * 200),(int)  (localidade.getY() * 200), 10, 10);
//                ourGraphics.drawString("Hotel em  " + localidade.getNome(), (int) (localidade.getX() * 200), (int) (localidade.getY() * 200));
//            }

            // make sure you restore the original transform or else the drawing
            // of borders and other components might be messed up
            ourGraphics.setTransform(saveTransform);
        }
        
        /**
         * Draw an arrow line between two points.
         * @param g the graphics component.
         * @param x1 x-position of first point.
         * @param y1 y-position of first point.
         * @param x2 x-position of second point.
         * @param y2 y-position of second point.
         * @param d  the width of the arrow.
         * @param h  the height of the arrow.
         */
        private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
            int dx = x2 - x1, dy = y2 - y1;
            double D = Math.sqrt(dx*dx + dy*dy);
            double xm = D - d, xn = xm, ym = h, yn = -h, x;
            double sin = dy / D, cos = dx / D;
            
            x = xm*cos - ym*sin + x1;
            ym = xm*sin + ym*cos + y1;
            xm = x;
            
            x = xn*cos - yn*sin + x1;
            yn = xn*sin + yn*cos + y1;
            xn = x;
            
            int[] xpoints = {x2, (int) xm, (int) xn};
            int[] ypoints = {y2, (int) ym, (int) yn};
            
            g.drawLine(x1, y1, x2, y2);
            g.fillPolygon(xpoints, ypoints, 3);
        }
        
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }
    }
    
    class PanningHandler implements MouseListener, MouseMotionListener {
        double referenceX;
        double referenceY;
        // saves the initial transform at the beginning of the pan interaction
        AffineTransform initialTransform;
        
        // capture the starting point
        public void mousePressed(MouseEvent e) {
            
            // first transform the mouse point to the pan and zoom
            // coordinates
            try {
                XFormedPoint = at.inverseTransform(e.getPoint(), null);
            } catch (NoninvertibleTransformException te) {
                System.out.println(te);
            }
            
            // save the transformed starting point and the initial
            // transform
            referenceX = XFormedPoint.getX();
            referenceY = XFormedPoint.getY();
            initialTransform = at;
        }
        
        public void mouseDragged(MouseEvent e) {
            
            // first transform the mouse point to the pan and zoom
            // coordinates. We must take care to transform by the
            // initial tranform, not the updated transform, so that
            // both the initial reference point and all subsequent
            // reference points are measured against the same origin.
            try {
                XFormedPoint = initialTransform.inverseTransform(e.getPoint(), null);
            } catch (NoninvertibleTransformException te) {
                System.out.println(te);
            }
            
            // the size of the pan translations
            // are defined by the current mouse location subtracted
            // from the reference location
            double deltaX = XFormedPoint.getX() - referenceX;
            double deltaY = XFormedPoint.getY() - referenceY;
            
            // make the reference point be the new mouse point.
            referenceX = XFormedPoint.getX();
            referenceY = XFormedPoint.getY();
            
            canvas.translateX += deltaX;
            canvas.translateY += deltaY;
            
            // schedule a repaint.
            canvas.repaint();
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
    }
    
    class ScaleHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int zoomPercent = slider.getValue();
            // make sure zoom never gets to actual 0, or else the objects will
            // disappear and the matrix will be non-invertible.
            canvas.scale = Math.max(0.00001, zoomPercent / 100.0);
            canvas.repaint();
        }
    }
}