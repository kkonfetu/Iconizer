package application;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Controler {

	//App
	private ArrayList<Icon> iconClassList = new ArrayList<Icon>();
	//private IconLoaderThread ILThread;
	private final int MAX_COLOR = 255; 
	//Settings
	private int partSize = 16; //размер иконок варки
	private int jump = 15; //брать каждый N пиксель
	private int change = 5; //% отклонения похожести;
	private File srcPath;// = "./src/one.jpg";
	private String newName = "";
	private String packName = "";
	
	public class Icon {
		
		private File iconFile;
		private int[] cRGB;
		private int[] cAvgRGB;
		
		
		public Icon(File iFile, int[] avgRGB){
			this.iconFile = iFile;
			this.cAvgRGB = avgRGB;
		}
		
		public float isSimilar(int[] RGB, float realPersent){
			
			int redRation = Math.abs(cAvgRGB[0] - RGB[0]);
			int greenRation = Math.abs(cAvgRGB[1] - RGB[1]);
			int blueRation = Math.abs(cAvgRGB[2] - RGB[2]);

			float ratio = redRation + greenRation + blueRation;
			
			if( ( redRation <= MAX_COLOR * realPersent ) &&
				( greenRation <= MAX_COLOR * realPersent )  &&
				( blueRation <= MAX_COLOR * realPersent ) ){
				//System.out.println("TRUE");
				return ratio;
			}
			//System.out.println("FALSE");
			return -1;
		}
		
		
	}
	
	public class IconLoaderThread extends Thread{
		private File RootPackFolder;
		private File[] iconList;
		private String packFolder;
		private boolean isGenerate;
		
		public IconLoaderThread( String path, boolean generate){
			packFolder = path;
			RootPackFolder = new File("./packs/"+packFolder);
			isGenerate = generate;
		}
		
	    //метод определения расширения файла
	    private String getFileExtension(File file) {
	        String fileName = file.getName();
	        // если в имени файла есть точка и она не является первым символом в названии файла
	        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
	        // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
	        return fileName.substring(fileName.lastIndexOf(".")+1);
	        // в противном случае возвращаем заглушку, то есть расширение не найдено
	        else return "";
	    }
		
		public void generateXML(){
			
			buildButton.setDisable(true);
			iconList = RootPackFolder.listFiles();
			
			iconClassList = new ArrayList<Icon>();
	        XMLOutputFactory output = XMLOutputFactory.newInstance();
	        XMLStreamWriter writer;
	        
	        try {
			writer = output.createXMLStreamWriter(new FileWriter(RootPackFolder+"/IconData.xml"));
	        //writer = output.createXMLStreamWrite(new FileWriter("IconData_p.xml"));
			writer.writeStartDocument("1.0");
			writer.writeStartElement("IconData");
			for(int i = 0; i < iconList.length; i++){
				
				//Это файл и он не xml
				if( iconList[i].isFile() && !getFileExtension(iconList[i]).equals(new String("xml")) ){
					
					File file = iconList[i];
					BufferedImage image = null;
					int[] rgb = {0, 0, 0}; // RGB одного пикселя
					int[] avgRBG = {0, 0, 0}; //сумма всех RGB пикселей (одной иконки)
					try {
						image = ImageIO.read(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					for(int ih=0; ih < image.getHeight(); ih++){
						for(int iw=0; iw < image.getWidth(); iw++){
							rgb = getRGB(image, iw, ih);
							avgRBG[0] += rgb[0];
							avgRBG[1] += rgb[1];
							avgRBG[2] += rgb[2];
						}
					}
					avgRBG[0] /= image.getWidth() * image.getHeight();
					avgRBG[1] /= image.getWidth() * image.getHeight();
					avgRBG[2] /= image.getWidth() * image.getHeight();
					

			        

						writer.writeCharacters("\r\n");
			            writer.writeStartElement("Icon");
			            	writer.writeStartElement("File");
			            		writer.writeCharacters(iconList[i].toString());
			            	writer.writeEndElement();
			            
			            	writer.writeCharacters("\r\n");
			            	
			            	writer.writeStartElement("R");
		            			writer.writeCharacters(String.valueOf(avgRBG[0]));
		            		writer.writeEndElement();
		            		
		            		writer.writeCharacters("\r\n");
		            		
			            	writer.writeStartElement("G");
	            				writer.writeCharacters(String.valueOf(avgRBG[1]));
	            			writer.writeEndElement();
	            			
	            			writer.writeCharacters("\r\n");
	            			
			            	writer.writeStartElement("B");
            					writer.writeCharacters(String.valueOf(avgRBG[2]));
            				writer.writeEndElement();
				            //writer.writeCharacters("1");
				            /*writer.writeAttribute("File", iconList[i].toString());
				            writer.writeAttribute("R", String.valueOf(avgRBG[0]) );
				            writer.writeAttribute("G", String.valueOf(avgRBG[1]) );
				            writer.writeAttribute("B", String.valueOf(avgRBG[2]) );*/
			            writer.writeEndElement();
			        	
			            // Закрываем тэг
			           // writer.writeEndElement();
					    // Закрываем XML-документ

						

					iconClassList.add(new Icon(file, avgRBG));
				}
			}
			writer.writeEndElement();
		    writer.writeEndDocument();
		    writer.flush();
		    
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        


	        buildButton.setDisable(false);
		}
		public void loadFromXML(){
			
			buildButton.setDisable(true);
			//iconClassList = new ArrayList<Icon>();
	        // Создается дерево DOM документа из файла
	        try {
	        	DocumentBuilder documentBuilder = null;
				try {
					documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Document document = documentBuilder.parse(RootPackFolder+"/IconData.xml");
				
	            // Получаем корневой элемент
	            Node root = document.getDocumentElement();

	            // Просматриваем все подэлементы корневого
	            NodeList icons = root.getChildNodes();
	            
	            for (int i = 0; i < icons.getLength(); i++) {
	                Node icon = icons.item(i);

	                // Если нода не текст, то заходим внутрь
	                if (icon.getNodeType() != Node.TEXT_NODE) {
	                	
		                int[] avgRBG = {0, 0, 0}; 
		                File newIconFile = null;
		                //boolean noewWrite = false;
		                
	                    NodeList iconProps = icon.getChildNodes();
	                    for(int j = 0; j < iconProps.getLength(); j++) {
	                        Node iconProp = iconProps.item(j);
	                        // Если нода не текст, то это один из параметров книги - печатаем
	                        if (iconProp.getNodeType() != Node.TEXT_NODE) {
	                        	
	                           // System.out.println(bookProp.getNodeName() + ":" + bookProp.getChildNodes().item(0).getTextContent());
	                        	if(iconProp.getNodeName().equals(new String("File"))){
	                        		newIconFile =  new File(iconProp.getChildNodes().item(0).getTextContent());
	                        	}
	                        	if(iconProp.getNodeName().equals(new String("R"))){
	                        		avgRBG[0] = Integer.valueOf( iconProp.getChildNodes().item(0).getTextContent() );
	                        	}
	                        	if(iconProp.getNodeName().equals(new String("G"))){
	                        		avgRBG[1] = Integer.valueOf( iconProp.getChildNodes().item(0).getTextContent() );
	                        	}
	                        	if(iconProp.getNodeName().equals(new String("b"))){
	                        		avgRBG[2] = Integer.valueOf( iconProp.getChildNodes().item(0).getTextContent() );
	                        	}
	                        }
	                    }
		                if(newIconFile != null){
		                	Icon ico = new Icon(newIconFile, avgRBG);
		                	iconClassList.add(ico);
		                	//System.out.println(">>>"+newIconFile+" "+avgRBG+" "+iconClassList.size());
		                }
	                    
	                }
	                

	            }
	            System.out.println("iconClassList len from loader XML"+ iconClassList.size());
	            // Просматриваем все подэлементы корневого - т.е. книги
	           // NodeList icons = root.getChildNodes();
	            System.out.println("in xml "+icons.getLength());
				
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        buildButton.setDisable(false);
		}
		public void run(){
			
			statusText.setText("Подгрузка иконок...");
			
			RootPackFolder = new File("./packs/"+packFolder);
			iconList = RootPackFolder.listFiles();
			
			System.out.println(iconList.length);
			
			File file = new File(RootPackFolder+"/IconData.xml");
			if ( ( file.exists() && file.isFile() ) && !isGenerate) {
				System.out.println("Load XML");
				loadFromXML();
			}else{
				System.out.println("Generate XML");
				generateXML();
				
			}
			

			statusText.setText("Подгрузка иконок завершена! Иконок: "+iconList.length);
			
			//buildButton.setDisable(false);

			
			Thread.currentThread().interrupt();
			return;
			
		}
		/*
		public void loadIconInfo(){

		}*/
	}
	public class ImageBuilderThread extends Thread{
		public ImageBuilderThread(){
			
		}
		public void run(){
			buildButton.setDisable(true);
			statusText.setText("Сбор иконок в картинку...");
			BufferedImage srcImage = null;
			//BufferedImage icoImage = null;
			

			try {
				srcImage = ImageIO.read(srcPath);
				//icoImage = ImageIO.read(new File("./src/ico.png"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Устанавливаем размер собранного изображения по установленным настройкам.
			BufferedImage outImage = new BufferedImage(
					(int) (srcImage.getWidth() / jump * partSize),
					(int)(srcImage.getHeight() / jump * partSize),
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = outImage.getGraphics();
			System.out.println("iconClassList.size()"+ iconClassList.size());
			for(int ih = 0; ih < srcImage.getHeight(); ih+=jump){
				for(int iw = 0; iw < srcImage.getWidth(); iw+=jump){
					int[] rgb = getRGB(srcImage, iw, ih);
					BufferedImage similarIcon = getSimilarIcon(iconClassList, rgb, change );
					g.drawImage(Scalr.resize(similarIcon, partSize), iw*partSize/jump, ih*partSize/jump, null);
					
				}
				float proccesValue = (float)ih / (float)srcImage.getHeight()*100;
				statusText.setText("Обработка изображения "+ String.format("%.1f", proccesValue)+"%");
				//System.out.println( ((float)ih / (float)srcImage.getHeight())*100 );
			}
			//packName
			// Save as new image
			try {
				ImageIO.write(outImage, "PNG", new File("./"+newName+".png"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			statusText.setText("Сбор завершён!");
			buildButton.setDisable(false);
			
			Thread.currentThread().interrupt();
			return;
			//myText.setText( String.valueOf(iconClassList.size()) ) ;
			
		}
	}
	
	int[] getRGB(BufferedImage image, int x, int y){
		//int  red   = (clr & 0x00ff0000) >> 16;
		//int  green = (clr & 0x0000ff00) >> 8;
		//int  blue  =  clr & 0x000000ff;

		int clr = image.getRGB(x,y);
		int[] rgb = {(clr & 0x00ff0000) >> 16, (clr & 0x0000ff00) >> 8, clr & 0x000000ff};
		return rgb;
	}
	
	public BufferedImage getSimilarIcon(ArrayList<Icon> icl, int[] RGB, int persentChange){
		
		// 0 - red
		// 1 - green
		// 2 - blue
		float realPersent = persentChange/100;
		BufferedImage rImage = null;
		
		float iconRatio = 9999;
		//file
		
		for(int i = 0; i < icl.size(); i++){
			Icon icon = icl.get(i);
			
			//Чем больше ratio тем хуже схожесть!
			float nowRatio = icon.isSimilar(RGB, realPersent);
			if( (nowRatio < iconRatio) && nowRatio >= 0){
				iconRatio = nowRatio;
				try {
					rImage = ImageIO.read(icon.iconFile);
					//return rImage;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			if( (i+1) == icl.size() && rImage == null){
				realPersent +=0.05; // Если нужной иконки не найдено, то увеличиваем разброс на 5%
				i = 0;
				iconRatio = 9999;
				//System.out.println("PERSENT UP");
			}
		}

		return rImage;
	}
	
	
	@FXML
	private Button buildButton;
	@FXML
	private Button choseButton;
	@FXML
	private Button reCompileIconDataButton;


	@FXML
	private Text statusText;
	@FXML
	private Text choserText;
	@FXML
	private Text iconSizeText;
	@FXML
	private Text outImgSizeText;
	
	@FXML
	private TextField iconSizeInput;
	@FXML
	private TextField jumpSizeInput;

	@FXML
	private BorderPane myBorderPane;
	@FXML
	private AnchorPane myAnchorPane;
	@FXML
	private MenuButton packMenuButton;
	
	//Перерасчёт полей в форме.
	public void reCalcInput(File srcPath){
		BufferedImage srcImage = null;
		//BufferedImage icoImage = null;
		if(srcPath != null)
		{
			try {
				srcImage = ImageIO.read(srcPath);
				//icoImage = ImageIO.read(new File("./src/ico.png"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newName = srcPath.getName().replace(".", "_");
			
			//System.out.println(str);
			int eIconSize = Integer.parseInt(iconSizeInput.getText());
			int eJumpSize = Integer.parseInt(jumpSizeInput.getText());
			
			newName = newName+"_icon"+eIconSize+"x"+eIconSize+"_jump"+eJumpSize+"_"+packName;
			iconSizeText.setText(eIconSize+" x "+eIconSize);
			outImgSizeText.setText( (srcImage.getWidth()*eIconSize/eJumpSize)+" x "+(srcImage.getHeight()*eIconSize/eJumpSize) );
			buildButton.setDisable(false);
		}else{
			buildButton.setDisable(true);
		}
		
	}
	
	@FXML
	private void initialize() {
		
		reCompileIconDataButton.setTooltip(new Tooltip("Обновляет пак иконок."));
		
		File RootPackFolder = new File("./packs/");
		
		File[] Packs = RootPackFolder.listFiles();
		for(int i = 0; i < Packs.length; i++){
			
			if(Packs[i].isDirectory()){
				MenuItem menuItem = new MenuItem( Packs[i].getName() );
				System.out.println( Packs[i].getName()  );
				
				menuItem.setOnAction((event) -> {
					
					
					MenuItem thisItem = (MenuItem)event.getTarget();
					packName = thisItem.getText();
					
					packMenuButton.setText( packName );
					IconLoaderThread ILThread = new IconLoaderThread( packName, false );
					ILThread.start();
					
				});
				
				packMenuButton.getItems().add( menuItem );
			}
		}
		
		buildButton.setDisable(true);
		jumpSizeInput.setDisable(true);
		iconSizeInput.setDisable(true);

		
		reCompileIconDataButton.setOnAction((event) -> {

			IconLoaderThread ILThread = new IconLoaderThread( packMenuButton.getText(), true );
			ILThread.start();
			//reCalcInput(srcPath);

		});
		

		iconSizeInput.setOnKeyReleased((event) -> {
			reCalcInput(srcPath);
		});
		jumpSizeInput.setOnKeyReleased((event) -> {
			reCalcInput(srcPath);
		});
		
		choseButton.setOnAction((event) -> {

			
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("IMG", "*.png", "*.jpg", "*.gif"));
			//fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
			srcPath = fileChooser.showOpenDialog(choseButton.getScene().getWindow());
			if( srcPath != null ){
				choserText.setText(srcPath.getAbsolutePath());
			}else{
				choserText.setText("Картинка не выбрана :(");
			}
			
			
			
			iconSizeInput.setDisable(false);
			jumpSizeInput.setDisable(false);
			
			reCalcInput(srcPath);
			// (srcImage.getWidth()*eIconSize/eJumpSize)+" x "+(srcImage.getHeight()*eIconSize/eJumpSize)
			
			
		});
		
		
		

		
		buildButton.setOnAction((event) -> {
			partSize = Integer.parseInt(iconSizeInput.getText());
			jump = Integer.parseInt(jumpSizeInput.getText());
			
			ImageBuilderThread IBThread = new ImageBuilderThread();
			IBThread.start();
			
		});
		
	}

	
	

	
}
