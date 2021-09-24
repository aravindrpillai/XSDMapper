
public class EcmRunner {

	public static void main(String[] args) {
		test4();
	}
	
	public static void test2() {
        XsdMapper.setGwXSDFileName("C:/ecm/sampletomap.xsd");
		XsdMapper.setClientXSDFileName("C:/ecm/sampletomap.xsd");
		XsdMapper.setOutputFile("C:/ecm/op.txt");
		XsdMapper.showXSDLines(false);
		XsdMapper.setXPathRequired(true);
		XsdMapper.generateMapping();

	}
	

	public static void test0() {
		XsdMapper.generateXPath("C:/ecm/entity.xsd");
	}

	public static void test1() {
		XsdMapper.generateXPath("C:/ecm/sampletomap.xsd");
	}

	
	public static void test3() {
		XsdMapper.setGwXSDFileName("C:/ecm/payload.xsd");
		XsdMapper.setClientXSDFileName("C:/ecm/payload.xsd");
		XsdMapper.setOutputFile("C:/ecm/op.txt");
		XsdMapper.showXSDLines(false);
		XsdMapper.setXPathRequired(true);
		XsdMapper.generateMapping();
	}
	
	public static void test4() {
		XsdMapper.showXSDLines(true);
		XsdMapper.generateXPath("C:/ecm/payload.xsd");
	}
	
}

