import java.io.*;
import java.math.*;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.net.URL;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import org.apache.commons.io.*;
import org.json.*;

public class JSONSelector 
{
    public static void main(String[] args) throws Exception
    {
    	JSONObject json = fetchJSON();
   		Scanner scanner = new Scanner(System.in);
   		while(scanner.hasNext())
    	{
   			String selector = scanner.next();
   			if(selector.equalsIgnoreCase("Exit"))
   				break;
   			printMatchingViews(selector, json);
    	}
   		scanner.close();
    }
    /*
     * Print all views matching the selector in the provided JSONObject, and their nested views
     * 
     */
    public static void printMatchingViews(String selector, JSONObject json)
    {
    	//Are we dealing with a selector chain and/or compound selector?
    	//Begin by assuming the selector is simply a class.
    	//Find index of "." and "#", determine which type of selector is first.
    	String viewClass = "";
    	String viewClassName = "";
    	String viewIdentifier = "";
    	int classSeparator = selector.indexOf(".");
		int viewSeparator = selector.indexOf("#");    	
    	if(classSeparator==0) // starts with className
    	{
    		if(viewSeparator>0)
    		{
    			viewClassName=selector.substring(classSeparator+1,viewSeparator);
    			viewIdentifier=selector.substring(viewSeparator+1);
    		}			
    		else
    			viewClassName=selector.substring(classSeparator+1);
    	}
    	else if(viewSeparator>0) //starts with identifier
    	{
    		if(classSeparator>0) //should not happen but just in case, "." is expected to precede "#"
    		{
    			viewClassName=selector.substring(classSeparator+1,viewSeparator);
    			viewIdentifier=selector.substring(viewSeparator+1);
    		}
    		else
    			viewIdentifier=selector.substring(viewSeparator+1);
    	}
    	else //starts with class.  "." is expected to precede "#"
    	{
    		viewClass = selector;
	    	if(classSeparator > -1)
	    	{
	    		viewClass = selector.substring(0,classSeparator);
	    		viewClassName = selector.substring(classSeparator+1);
	    	}
			if(viewSeparator > -1)
			{
				if(viewClassName.equalsIgnoreCase("")) //only class and identifier present
				{
					viewClass = selector.substring(0, viewSeparator);
					viewIdentifier = selector.substring(viewSeparator+1);
				}
				else
				{
					viewClassName = selector.substring(classSeparator+1,viewSeparator);
					viewIdentifier = selector.substring(viewSeparator+1);	
				}
			}
    	}
		
		
		String[] attributes = JSONObject.getNames(json);
		for (String attribute : attributes)
		{
			//Recurse for subviews.  May be an array of nodes
		    if("subviews".equalsIgnoreCase(attribute.toString()))
		    {
			    Object subViews = json.get(attribute);
			    Iterator<Object> i = ((JSONArray)subViews).iterator();
			    while(i.hasNext())
			    {
			    	JSONObject subView = (JSONObject)i.next();
			    	printMatchingViews(selector, subView);
			    }

		    }
		    //Recurse for contentView or control.  Both only contain one node
		    else if("contentView".equalsIgnoreCase(attribute.toString()) 
		    		|| "control".equalsIgnoreCase(attribute.toString()))
		    {
		    	Object contentView = json.get(attribute);
		    	printMatchingViews(selector,(JSONObject)contentView);
		    }
		}
    	

		if(json.has("class") && (viewClass.isBlank() || viewClass.equalsIgnoreCase(json.get("class").toString())))
		{
			boolean classNameMatch = true;
			boolean viewIdentifierMatch = true;
			if(!viewClassName.isBlank())
			{
				classNameMatch = false;
				if(json.has("classNames"))
				{
					Object classNames = json.get("classNames");
					Iterator<Object> i = ((JSONArray)classNames).iterator();
					while(i.hasNext())
					{
						String className = (String)i.next();
						if(className.equalsIgnoreCase(viewClassName))
						{
							//print entire view if a selector chain is passed in, 
							classNameMatch=true;
							break;
						}
					}
				}
			}
			if(!viewIdentifier.isBlank() 
					&& (!json.has("identifier") 
							|| !viewIdentifier.equalsIgnoreCase(json.getString("identifier"))))
			{
				viewIdentifierMatch = false;
			}			
			if(classNameMatch && viewIdentifierMatch)
			{
				System.out.println("---");
				System.out.println(json.toString(2));
				System.out.println("---");
			}
		}
		
    }
    public static JSONObject fetchJSON() throws Exception
    {
    	return new JSONObject(IOUtils.toString(new URL("https://raw.githubusercontent.com/jdolan/quetoo/master/src/cgame/default/ui/settings/SystemViewController.json"), Charset.forName("UTF-8")));
    }
};