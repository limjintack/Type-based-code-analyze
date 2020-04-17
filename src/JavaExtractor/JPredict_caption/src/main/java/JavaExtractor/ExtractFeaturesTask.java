package JavaExtractor;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.FeaturesEntities.ProgramFeatures;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.json.simple.*;
import org.json.simple.parser.*;

class ExtractFeaturesTask implements Callable<Void> {
    private final CommandLineValues m_CommandLineValues;
    private final Path filePath;

    public ExtractFeaturesTask(CommandLineValues commandLineValues, Path path) {
        m_CommandLineValues = commandLineValues;
        this.filePath = path;
    }

    @Override
    public Void call() {
        processFile();
        return null;
    }

    public void processFile() {

        JSONParser parser = new JSONParser();
        FeatureExtractor featureExtractor = new FeatureExtractor(m_CommandLineValues);
    	try { 
        	Object file = parser.parse(new FileReader("D:\\data\\test.dataset")); 
        	JSONArray jsonData = (JSONArray) file;
        	for (int i = 0; i<jsonData.size(); i++)
        	{
            	//System.out.println(jsonData.get(i));
            	ProgramFeatures features;
                features = featureExtractor.extractFeatures((JSONObject) jsonData.get(i));
                if (features == null) {
                    continue;
                }
                String toPrint = featuresToString(features);
                if (toPrint.length() > 0) {
                    System.out.println(toPrint);
                }
        	}
        } 
        catch (FileNotFoundException e) {
        	e.printStackTrace(); 
        } 
        catch (IOException e) { 
        	e.printStackTrace(); 
        } 
        catch (ParseException e) { 
        	e.printStackTrace(); 
        }
    	
    	
        
    }


    public String featuresToString(ProgramFeatures features) {
        if (features == null || features.isEmpty()) {
            return Common.EmptyString;
        }

        List<String> methodsOutputs = new ArrayList<>();

        String toPrint = features.toString();
        if (m_CommandLineValues.PrettyPrint) {
            toPrint = toPrint.replace(" ", "\n\t");
        }
        return toPrint;
    }
}
