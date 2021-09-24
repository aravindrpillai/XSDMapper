import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Author : Aravind R Pillai
 * Date   : 12 Dec, 2017
 * Desc   : Maps fields between 2 xsds 
 * Note   : XSD should not contain any empty spaces or lines in between
 * Guidewire Usage : 		 
 		uses mapping.ECM
 		ECM.setGwXSDFileName("C:/ecm/entity.xsd");
		ECM.setClientXSDFileName("C:/ecm/sampletomap.xsd");
		ECM.setOutputFile("C:/ecm/op.txt");
		ECM.setShowGwXSDReadX(true); //default is falses
		ECM.generateMapping();
 */

public class XsdMapper {
    
	static HashMap<Integer, List> child;
	static HashMap<String, String> parentPathOnGwXSD;
	static List<String> xsdFileLines;    
	static String gwXSDFileName = null;
	static String clientXSDFileName = null;
	static String outputFile = null;
	static Boolean showXSD = false;
	static Boolean xPath = false;
	
	public static void setGwXSDFileName(String fn)     { gwXSDFileName     = fn;   }
	public static void setClientXSDFileName(String pn) { clientXSDFileName = pn;   }
	public static void setOutputFile(String op)        { outputFile        = op;   }
	public static void showXSDLines(Boolean disp)      { showXSD           = disp; }
	public static void setXPathRequired(Boolean xp)    { xPath             = xp;   }
	

	
    public static void generateXPath(String filePath) {
        try{
	    	if(filePath == null){
	    		throw new Exception("Files Not Specified");
	    	}
	    	setXPathRequired(true);
	    	HashMap<String, String> parentMapOp = createParentHirarchyMapForXSD(filePath);
	    	String content = convertToString(parentMapOp);
	    	System.out.println(content);
	    	if(outputFile != null) {
	    		writeToFile(content);
	    	}
	   }catch(Exception e){
    		System.out.println("Error : "+e);
    	}
    }
    
    
    public static void generateMapping() {
    	try{
    		if((clientXSDFileName == null) || (gwXSDFileName == null)){
	    		throw new Exception("Files Not Specified");
	    	}
	    	
    		setXPathRequired(true);
	    	HashMap<String, String> gwXsd     = createParentHirarchyMapForXSD(gwXSDFileName);
	    	HashMap<String, String> clientXsd = createParentHirarchyMapForXSD(clientXSDFileName);
	    	HashMap<String, String> output = new HashMap<>();
	    	for(String key : gwXsd.keySet()) {
	    		output.put(gwXsd.get(key), clientXsd.get(key));
	    	}
	    	
	    	String content = convertToString(output);
	    	
	    	System.out.println(content);
	    	if(outputFile != null) {
	    		writeToFile(content);
	    	}
	   }catch(Exception e){
    		System.out.println("Error : "+e);
    	}
    }

   
    
    private static HashMap<String, String> createParentHirarchyMapForXSD(String xsd) {
    	try{
	    	Charset charset = Charset.forName("ISO-8859-1");
	    	xsdFileLines = Files.readAllLines(Paths.get(xsd), charset);
	    	return readXMLTagsFromXsd();
	        
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
		return null;
    }

    
    /*
    * Function Maps the starting and ending of tags in GW XSD
    */
	private static HashMap<String, String>  readXMLTagsFromXsd() throws Exception{
    
		HashMap<Integer, Integer> mapper = new HashMap<Integer, Integer>();
        String line;
        
        List<String> lines = new ArrayList<String>();

        for(int i=0; i<xsdFileLines.size(); i++){
            line = xsdFileLines.get(i).trim();
        
            if(!(line.isEmpty() || line.contains("<?") || line=="" || line==" " || line==null)) {
            	lines.add(line);
            
	            if(showXSD) System.out.println("Line "+i+" : "+line);
	            
	            if(line.contains("<") ){
		            if((line.contains("/")) && (line.contains("name="))){
		              mapper.put(i,i); 
		            }else{ 
		              if((line.contains("/")) && (!line.contains("name="))) mapper.put(i,-1); //ending statement
		              else                                                  mapper.put(i,-2); //starting statement
		            }
		            
		        }
            }
        }
        
        xsdFileLines.clear();
        xsdFileLines = lines;

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
        
        
       return findParents(mapper); 
    }

	
	
    /*
    * Function read the line and map the parents
    */
    private static HashMap<String, String> findParents(HashMap<Integer, Integer> mapper) throws Exception{
        
    	List<Integer> parentsList;
        HashMap<Integer, List<Integer>> child = new HashMap<Integer, List<Integer>>();
        int count = 1;
        int k;
        int v;
        for (Integer key: mapper.keySet()){
        	k = (int)key;
        	v = (int)mapper.get(key);
        	parentsList = new ArrayList<>();
        	if(k == v){	
	        	for(int j=(k-1); j>=0; j--){
	        	   if(mapper.get(j) != j){
	             	 if(mapper.get(j) > j){
	                 	if(mapper.get(j) > k){
	                 	   if(xsdFileLines.get(j).contains("name=")){
	                           parentsList.add(j);
	                       }   
	                    }
	                  }  
	                }
	            }
	        	count +=1;
	        	child.put(k,parentsList);
        	}
        } 
        System.out.println(" * "+count+" nodes found\n");
        return createParentPathFromXSD(child);  
    }
    
    
    
    /*
    * Function records the mapping
    */
    private static HashMap<String, String> createParentPathFromXSD(HashMap<Integer, List<Integer>> child) throws Exception{
    
    	HashMap<String, String> parentPathOnXSD = new HashMap<>();
        Integer key;
        List<Integer> value;
        String valueAppender;
        
        for (Map.Entry<Integer,List<Integer>> entry : child.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();           
            valueAppender = "";
            for(int i=((value.size())-1); i>=0; i--){
                valueAppender += getNameValue((int) value.get(i));
                valueAppender += (xPath ? "/":"."); 
                if(i == 0) { 
                  valueAppender += getNameValue((int) key);
                }
            }
            
            parentPathOnXSD.put(createKey(getNameValue(key),valueAppender,parentPathOnXSD),valueAppender);
            /*if(parentPathOnXSD.containsKey(getNameValue(key))) {
            	String[] splitValue = valueAppender.split(xPath?"/":".");
            	int len = splitValue.length-2; 
            	parentPathOnXSD.put(((splitValue[len]))+(xPath?"/":".")+getNameValue(key),valueAppender);
            }else {
            	parentPathOnXSD.put(getNameValue(key),valueAppender);
            }*/
        }
    
        return parentPathOnXSD;
    }
    
    
    private static String createKey(String key, String value, HashMap<String, String> map) {
    	String delimitor = (xPath?"/":".");
    	String[] splitValue = value.split(delimitor);
    	int index = (key.contains(delimitor)) ? splitValue.length-3 : splitValue.length-2; 
    	if(map.containsKey(key)){
    		return splitValue[index] + delimitor + key;
    	}else {
    		return key;
    	}
    }
    
   /*
    * Returns the name value from line
    */
    private static String getNameValue(int lineNo) throws Exception{
        
        String[] lineSplit = (xsdFileLines.get(lineNo)).split("name=");
        if(lineSplit.length == 2){
            String[] split = lineSplit[1].split("\"");
            String required = split[1];
            required = required.substring(0, 1).toUpperCase()+required.substring(1);
            return required;
        }else{
            throw new Exception("No Name Value Found For Line : "+xsdFileLines.get(lineNo));
        }
    }
    
    
    
    private static void writeToFile(String content) throws IOException {
    	FileWriter fileWriter = null;
		fileWriter = new FileWriter(new File(outputFile));
	    fileWriter.write(content);
		fileWriter.append(design);
		fileWriter.close();
	}

    
    private static String convertToString(HashMap<String, String> input) {
    	String output = "";
    	for (String key: input.keySet()){
    		output += (key+" == "+input.get(key)+"\n");
    	}
    	return output;
    }
    
}


