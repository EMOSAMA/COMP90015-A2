package COMM;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileOperation {
    private JFileChooser fileChooser;
    private String filePath;
    private File file;

    public class ImgFileFilter extends FileFilter{
        String extension;
        String description;
        public ImgFileFilter (String extenstion, String description) {
            this.extension = extenstion;
            this.description = description;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            String fileName = f.getName();
            if (fileName.toUpperCase().endsWith(this.extension.toUpperCase())) {
                return true;
            }else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public String getExtension() {
            return this.extension;
        }
    }

    public FileOperation(){
        fileChooser = new JFileChooser();
        ImgFileFilter jpgFilter = new ImgFileFilter(".jpg", "*.jpg");
        ImgFileFilter imgFilter = new ImgFileFilter(".jpeg", "*.jpeg");
        ImgFileFilter pngFilter = new ImgFileFilter(".png", "*.png");
        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.addChoosableFileFilter(imgFilter);
        fileChooser.addChoosableFileFilter(pngFilter);
    }

    public void newFile(){
        filePath = null;
    }

    public BufferedImage openFile(){
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileChooser.setCurrentDirectory(new File("/"));
            filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (filePath == null) {
                return null;
            }
            else {
                file=new File(filePath);
            }
            try {
                BufferedImage bufImage = ImageIO.read(file);
                return bufImage;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public void saveFile(BufferedImage image){
        fileChooser.setVisible(true);
        if (filePath == null) {
            saveFileAs(image);
            return;
        }
        else {
            file = new File(filePath);
        }
        try {
            String[] format = filePath.split("\\.");
            ImageIO.write(image, format[format.length - 1],file);
            JOptionPane.showMessageDialog(null, "save success", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void saveFileAs(BufferedImage image){
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileChooser.setCurrentDirectory(new File("/"));
            String extension;
            try {
                ImgFileFilter filter = (ImgFileFilter)fileChooser.getFileFilter();
                extension = filter.getExtension();
            }
            catch(Exception e2) {
                extension = ".png";
            }
            file = fileChooser.getSelectedFile();
            File newFile = null;
            try {
                if (file.getAbsolutePath().toUpperCase().endsWith(extension.toUpperCase())) {
                    newFile = file;
                    filePath = file.getAbsolutePath();
                } else {
                    newFile = new File(file.getAbsolutePath() + extension);
                    filePath = file.getAbsolutePath() + extension;
                }
                extension = extension.substring(1);
                ImageIO.write(image,extension, newFile);
                JOptionPane.showMessageDialog(null, "save success", "Information", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
