package com.Makylone.clawerichika.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public class AnimatedWheelGenerator {

    // Taille de l'image (Carré de 500x500 pixels)
    private static final int SIZE = 500; 
    private static final int CENTER = SIZE / 2;

    // Palette de couleurs pour les parts de la roue
    private static final Color[] COLORS = {
        new Color(231, 76, 60),  // Rouge
        new Color(52, 152, 219), // Bleu
        new Color(46, 204, 113), // Vert
        new Color(241, 196, 15), // Jaune
        new Color(155, 89, 182), // Violet
        new Color(230, 126, 34)  // Orange
    };
    
    // Liste des options disponibles
    public static final String[] OPTIONS = {
        "Timeout 5 min", "Timeout 10 min", "Timeout 1h", "Reset les perms", "Reverse"
    };

    /**
     * ÉTAPE 1 : PRÉ-RENDU
     * Cette méthode dessine la roue "à plat" (angle 0).
     * On le fait une seule fois pour économiser le CPU.
     */
    private static BufferedImage generateStaticWheelImage() {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Active l'anti-aliasing pour que les courbes et le texte soient lisses (pas d'effet escalier)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int count = OPTIONS.length;
        // Calcul de la taille d'une part en degrés. Ex: 5 options = 360 / 5 = 72 degrés par part.
        double anglePerSlice = 360.0 / count;
        double currentAngle = 0; // Angle de départ pour la boucle
        int wheelRadius = (SIZE - 40) / 2;
        int textDistance = (int) (wheelRadius * 0.70);

        for (int i = 0; i < count; i++) {
            // 1. Choix de la couleur (modulo pour boucler sur le tableau de couleurs)
            g2d.setColor(COLORS[i % COLORS.length]);

            // 2. Dessin de la part de "pizza" (Arc)
            // Arc2D.PIE signifie que l'arc est relié au centre (forme de camembert)
            Arc2D arc = new Arc2D.Double(20, 20, SIZE - 40, SIZE - 40, currentAngle, anglePerSlice, Arc2D.PIE);
            g2d.fill(arc);

            // 3. Placement du texte
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16)); 
            FontMetrics metrics = g2d.getFontMetrics();
            String text = OPTIONS[i];
            // A. On cherche l'angle qui passe pile au MILIEU de la part
            double midAngle = currentAngle + (anglePerSlice / 2);
            
            // B. Conversion Degrés -> Radians
            // Les fonctions Java Math.cos() et Math.sin() ne comprennent que les Radians.
            // Formule : Radians = Degrés * (PI / 180)
            double midRad = Math.toRadians(midAngle);

            // B. SAUVEGARDE de l'état actuel (feuille droite)
            AffineTransform originalTransform = g2d.getTransform();

            // C. ROTATION de la feuille autour du centre
            // On tourne le graphique pour que l'axe X s'aligne avec le milieu de la part.
            // Attention : Dans Java2D, les angles positifs tournent dans le sens horaire,
            // mais notre calcul d'angle est trigonométrique (anti-horaire). Il faut donc inverser le signe (-midRad).
            g2d.rotate(-midRad, CENTER, CENTER);

            // D. Coordonnées Polaires vers Cartésiennes (X, Y)
            // X : On part du CENTER et on va jusqu'à textDistance. On retire la moitié de la largeur du texte pour le centrer.
            int drawX = CENTER + textDistance - (metrics.stringWidth(text) / 2);
            // Formule Y : Centre - Rayon * sin(angle)
            // ATTENTION : En maths classiques, Y monte. En informatique, Y descend.
            // Y : On reste sur la ligne centrale (CENTER), et on ajuste légèrement pour centrer verticalement.
            int drawY = CENTER - (metrics.getAscent() / 3);
            g2d.drawString(text, drawX, drawY);

            // E. RESTAURATION de la feuille (pour la prochaine part)
            g2d.setTransform(originalTransform);

            // On avance l'angle pour la prochaine part
            currentAngle += anglePerSlice;
        }
        g2d.dispose();
        return image;
    }

    /**
     * ÉTAPE 2 : ROTATION D'UNE IMAGE
     * Cette méthode prend l'image statique et la fait tourner.
     * C'est beaucoup plus rapide que de tout redessiner.
     */
    private static BufferedImage drawRotatedFrame(BufferedImage staticWheel, double rotationAngle) {
        BufferedImage frame = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = frame.createGraphics();

        // 1. Sauvegarde de la matrice de transformation actuelle (état "normal" du plan)
        AffineTransform old = g2d.getTransform();

        // 2. Rotation du plan de travail
        // On dit à Java : "Fais tourner la feuille autour du point central (CENTER, CENTER)"
        g2d.rotate(Math.toRadians(rotationAngle), CENTER, CENTER);

        // 3. On dessine l'image de la roue sur la feuille qui tourne
        g2d.drawImage(staticWheel, 0, 0, null);

        // 4. Restauration du plan (état "normal")
        // On remet la feuille droite pour dessiner le pointeur.
        // Si on ne faisait pas ça, le pointeur tournerait ave la roue
        g2d.setTransform(old);

        // 5. Dessin du Pointeur (Triangle Rouge fixe)
        g2d.setColor(Color.RED);
        int[] xPoints = {CENTER - 15, CENTER + 15, CENTER}; // Gauche, Droite, Pointe
        int[] yPoints = {10, 10, 40}; // Haut, Haut, Bas
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Rond central
        g2d.setColor(Color.WHITE);
        g2d.fillOval(CENTER - 20, CENTER - 20, 40, 40);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3)); // Épaisseur du contour
        g2d.drawOval(CENTER - 20, CENTER - 20, 40, 40);

        g2d.dispose();
        return frame;
    }

   public static byte[] generateGifWithFFmpeg(int winnerIndex) throws IOException, InterruptedException {
        
        Path tempDir = Files.createTempDirectory("clawerichika_wheel_");
        File outputFile = tempDir.resolve("output.gif").toFile();

        try {
            BufferedImage staticWheel = generateStaticWheelImage();

            // --- 1. CALCUL DE L'ANGLE CIBLE PRÉCIS ---
            double anglePerSlice = 360.0 / OPTIONS.length;
            
            // L'angle où se trouve le gagnant au départ (0°)
            double winnerStartAngle = winnerIndex * anglePerSlice + anglePerSlice / 2;
            
            // On calcule la rotation nécessaire pour amener l'angle du gagnant à 90°.
            // Formule : AngleGagnant - 90
            double rotationNeeded = winnerStartAngle - 90;
            
            // Normalisation pour que ce soit positif
            while (rotationNeeded < 0) rotationNeeded += 360;
            
            // On ajoute 4 tours complets (1440°) pour le spectacle
            // C'est la distance EXACTE que la roue va parcourir.
            double totalDistance = 1440 + rotationNeeded;

            // --- 2. BOUCLE D'ANIMATION BASÉE SUR LE TEMPS ---
            
            int totalFrames = 120; // La roue tournera pendant exactement 120 images (~5 sec)
            
            for (int frame = 0; frame <= totalFrames; frame++) {
                
                // On calcule le pourcentage d'avancement (de 0.0 à 1.0)
                double progress = (double) frame / totalFrames;
                
                // FONCTION D'EASING (LISSAGE)
                // On utilise une courbe "EaseOutQuart".
                // Ça commence très vite et ça freine doucement à la fin.
                // Formule : 1 - (1 - t)^4
                double ease = 1 - Math.pow(1 - progress, 4);
                
                // L'angle actuel est simplement la distance totale * le pourcentage lissé
                double currentAngle = totalDistance * ease;

                // Génération et sauvegarde
                BufferedImage image = drawRotatedFrame(staticWheel, currentAngle);
                File frameFile = tempDir.resolve(String.format("frame_%03d.png", frame)).toFile();
                ImageIO.write(image, "png", frameFile);
            }

            // --- 3. PAUSE DE FIN (3 SECONDES) ---
            // On reprend la dernière image calculée (qui est pile sur la cible)
            BufferedImage finalFrame = drawRotatedFrame(staticWheel, totalDistance);
            int pauseFrames = 25 * 3; // 25 fps * 3 sec = 75 frames
            
            // On continue la numérotation là où on s'est arrêté (totalFrames + 1)
            for (int i = 1; i <= pauseFrames; i++) {
                 File frameFile = tempDir.resolve(String.format("frame_%03d.png", totalFrames + i)).toFile();
                 ImageIO.write(finalFrame, "png", frameFile);
            }

            // --- 4. ENCODAGE FFMPEG ---
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", 
                "-y", 
                "-framerate", "25", 
                "-i", tempDir.resolve("frame_%03d.png").toString(), 
                "-vf", "split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse",
                outputFile.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS);

            return Files.readAllBytes(outputFile.toPath());

        } finally {
            // Nettoyage...
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            } catch (Exception ignored) {}
        }
    }
}