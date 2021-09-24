
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Author : Aravind R Pillai
 * Date   : 12 Jan, 2017
 * Desc   : Maps fields between 2 xsds 
 * Note   : XSD should not contain any empty spaces or lines in between
 * Guidewire Usage : 		 
 		uses mapping.ECM
 		ECM.setGwXSDFileName("C:/ecm/entity.xsd");
		ECM.setClientXSDFileName("C:/ecm/sampletomap.xsd");
		ECM.setOutputFile("C:/ecm/op.txt");
		ECM.setShowGwXSDReadX(true); //default is false
		ECM.generateMapping();
 */

public class ECM {
    
	static HashMap<Integer, List> child;
	static HashMap<String, String> parentPathOnGwXSD;
	static HashMap<Integer, Integer> mapper;
	static List<String> gwXSDFileLines;    
	static String gwXSDFileName = null;
	static String clientXSDFileName = null;
	static String outputFile = null;
	static Boolean showXSD = false;
	static Boolean xPath = false;
	
	public static void setGwXSDFileName(String fn)     { gwXSDFileName     = fn;   }
	public static void setClientXSDFileName(String pn) { clientXSDFileName = pn;   }
	public static void setOutputFile(String op)        { outputFile        = op;   }
	public static void setShowGwXSDReadX(Boolean disp) { showXSD           = disp; }
	public static void setXPathRequired(Boolean xp)    { xPath             = xp;   }
	

	
    public static void generateMapping() {
          
    	try{
	    	if((clientXSDFileName == null) || (gwXSDFileName == null)){
	    		throw new Exception("Files Not Specified");
	    	}
	    	
	    	Charset charset = Charset.forName("ISO-8859-1");
	        gwXSDFileLines = Files.readAllLines(Paths.get(gwXSDFileName), charset);
	        	        
	        readXMLTagsFromGwXSD();
	        findParents();
	        createParentPathOnGwXSD();
	        mapFiles();
    	}catch(Exception e){
    		System.out.println("Error : "+e);
    	}
    }

    
    /*
    * Function Maps the starting and ending of tags in GW XSD
    */
	private static void readXMLTagsFromGwXSD(){
    
        mapper = new HashMap<>();
        String line;
        
        for(int i=0; i<gwXSDFileLines.size(); i++){
            line = gwXSDFileLines.get(i).trim();
            
            if(showXSD) {
            	System.out.println(line);
            }
            
            if(line.contains("<")){
	            if((line.contains("/")) && (line.contains("name="))){
	              mapper.put(i,i); 
	            }else{ 
	              if((line.contains("/")) && (!line.contains("name="))) mapper.put(i,-1); //ending statement
	              else                                                  mapper.put(i,-2); //starting statement
	            }
            }
        }
        
        int mapperSize = mapper.size();
        
        try{
            for(int i=0; i<mapperSize; i++){
              if( mapper.get(i) == (-1) ){
                for(int j=(i-1); j>=0; j--){
                  if( mapper.get(j) == (-2) ){
                      mapper.put(i, j);
                      mapper.put(j, i);
                      break;
                  }    
                }
              }  
            }
        }catch(Exception e){} //Hiding Exception | Not required | Exception may happen     
    }

	
	
    /*
    * Function read the line and map the parents
    */
    private static void findParents() throws Exception{
        int mapperSize = mapper.size();
        List<Integer> parentsList;
        child = new HashMap<>();
        int lineJ = 0;
        for(int i=0; i<mapperSize; i++){
            
         parentsList = new ArrayList<>();
            
         if(mapper.get(i) == i){
           for(int j=(i-1); j>=0 ;  j--){
             lineJ = mapper.get(j);
        	   if((lineJ != j) && (lineJ > j) && (lineJ > i)){
                    if(gwXSDFileLines.get(j).contains("name=")){
                        parentsList.add(j);
                    }   
                 }
           }
         child.put(i,parentsList);
         }
        }     
    }
    
    
    
    /*
    * Function records the mapping
    */
    private static void createParentPathOnGwXSD() throws Exception{
    
        parentPathOnGwXSD = new HashMap<>();
        Integer key;
        List value;
        String valueAppender;
        
        for (Map.Entry<Integer,List> entry : child.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();           
            valueAppender = "";
            for(int i=((value.size())-1); i>=0; i--){
                valueAppender += getNameValue((int) value.get(i));
                valueAppender += (xPath) ? "/":"."; 
                if(i == 0) { 
                  valueAppender += getNameValue((int) key);
                }
            }
            
            parentPathOnGwXSD.put(getNameValue(key),valueAppender);
        }
    
    }
    
    
    
    
   /*
    * Returns the name value from line
    */
    private static String getNameValue(int lineNo) throws Exception{
        
        String[] lineSplit = (gwXSDFileLines.get(lineNo)).split("name=");
        if(lineSplit.length == 2){
            String[] split = lineSplit[1].split("\"");
            String required = split[1];
            required = required.substring(0, 1).toUpperCase()+required.substring(1);
            return required;
        }else{
            throw new Exception("No Name Value Found For Line : "+gwXSDFileLines.get(lineNo));
        }
    }
    
  

    /*
    * Function displays final mapping output
    */  
    private static void mapFiles() throws IOException {
		
		FileInputStream fstream = new FileInputStream(clientXSDFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		
		FileWriter fileWriter = null;
		if(outputFile != null){
			File fw = new File(outputFile);
	        fileWriter = new FileWriter(fw);
	        fileWriter.write(" ");
		}
		
		String strLine;
		String head = "";
		String op;
		
		while ((strLine = br.readLine()) != null)   {
		 
		String[] splitLine = strLine.split("name=");
		
		if(splitLine.length >= 2){
			String req = splitLine[1].split(" ")[0];
			req = req.substring(1,(req.length())-1);
			int count = req.split("_").length;
			
			if(count == 1){
				
				String mapping = parentPathOnGwXSD.get(req);
				op = head+((xPath)?"/":".")+req+" = "+mapping;
								
				System.out.println (op);
				
				if(outputFile != null){
					fileWriter.append("\n"+op);
				}
				
			}else{
				head = req;
			}
		}
		}
		
		
		if(outputFile != null){
			fileWriter.close();
		}
		
		br.close();

	}
    
    
    

    
}


