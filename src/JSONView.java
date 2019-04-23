import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.io.Serializable;
import java.io.ObjectInputStream;
public class JSONView implements Serializable
{
	String clazz;
	String className;
	String identifier;
	JSONView subViews[];
}
