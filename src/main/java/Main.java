import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String JSONFILE1 = "data.json";
    public static final String JSONFILE2 = "data2.json";

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        //Task1 CSV to JSON
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, JSONFILE1);

        //Task2 XML to JSON
        list = parseXML("data.xml", columnMapping);
        json = listToJson(list);
        writeString(json, JSONFILE2);

        //Task3 JSON parser
        json = readString("new_data.json");
        list = jsonToList(json);
        list.stream().forEach(System.out::println);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping(columnMapping);
        List<Employee> employeeList = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            employeeList = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build()
                    .parse();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return employeeList;
    }

    public static String listToJson(List<Employee> employeeList) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(employeeList, listType);
    }

    public static void writeString(String jsonString, String file) {
        try (FileWriter jsonWriter = new FileWriter(file)) {
            jsonWriter.write(jsonString);
            jsonWriter.flush();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static List<Employee> parseXML(String fileName, String[] columnMapping) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(fileName));

        List<Employee> employeeList = new ArrayList<>();

//Тоже рабочий вариант преобразования xml в List<Employee>

//        NodeList id = document.getElementsByTagName(columnMapping[0]);
//        NodeList firstName = document.getElementsByTagName(columnMapping[1]);
//        NodeList lastName = document.getElementsByTagName(columnMapping[2]);
//        NodeList country = document.getElementsByTagName(columnMapping[3]);
//        NodeList age = document.getElementsByTagName(columnMapping[4]);
//
//        for (int i = 0; i < id.getLength(); i++) {
//            employeeList.add(
//                    new Employee(
//                            Long.parseLong(id.item(i).getTextContent())
//                            , firstName.item(i).getTextContent()
//                            , lastName.item(i).getTextContent()
//                            , country.item(i).getTextContent()
//                            , Integer.parseInt(age.item(i).getTextContent())
//                    ));
//        }

        NodeList employeesNode = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < employeesNode.getLength(); i++) {
            NodeList employeeValues = employeesNode.item(i).getChildNodes();
            String[] employeeArguments = new String[columnMapping.length];
            if (i % 2 != 0) {
                int diff = 1;
                for (int j = 0; j < employeeValues.getLength(); j++) {
                    if (j % 2 != 0) {
                        employeeArguments[j - diff] = employeeValues.item(j).getTextContent();
                        diff++;
                    }
                }
                employeeList.add(
                        new Employee(
                                Long.parseLong(employeeArguments[0])
                                , employeeArguments[1]
                                , employeeArguments[2]
                                , employeeArguments[3]
                                , Integer.parseInt(employeeArguments[4]))
                );
            }
        }
        return employeeList;
    }

    public static String readString(String fileName) {
        String jsonString = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder stringBuilder = new StringBuilder();
            String row;
            while ((row = bufferedReader.readLine()) != null) {
                stringBuilder.append(row);
            }
            jsonString = stringBuilder.toString();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return jsonString;
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(json);
            Gson gson = new GsonBuilder().create();
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                Employee employee = gson.fromJson(jsonObject.toJSONString(), Employee.class);
                employeeList.add(employee);
            }
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
        }
        return employeeList;
    }
}