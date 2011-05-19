package edu.stanford.prpl.insitu.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InSituUtils {

	public static boolean isEmailAddress(String input)
	{
		if (input == null || input.trim().length() <= 0)
			return false;
		
		Pattern pattern 
		= Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)");
		// "[\S]+@[\w-]+(.[\w-]+)+"

		try {
			Matcher matcher = pattern.matcher(input);	 
			if (matcher.matches())
				return true;
			else
				return false;
		}
		catch (Exception e) {
			return false;
		}
	}
	
}
