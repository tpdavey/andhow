package yarnandtail.andhow.internal;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import yarnandtail.andhow.AppFatalException;
import yarnandtail.andhow.ConstructionProblem;
import yarnandtail.andhow.Loader;
import yarnandtail.andhow.LoaderValues;
import yarnandtail.andhow.NamingStrategy;
import yarnandtail.andhow.PropertyValue;
import yarnandtail.andhow.PropertyValueProblem;
import yarnandtail.andhow.RequirementProblem;
import yarnandtail.andhow.ValueMapWithContext;
import yarnandtail.andhow.PropertyGroup;

/**
 * Utilities used by AndHow during initial construction.
 * @author eeverman
 */
public class AndHowUtil {
	

	/**
	 * Build a fully populated RuntimeDefinition from the points contained in
 the passed Groups, using the NamingStrategy to generate names for each.
	 * 
	 * @param groups The ConfigPointGroups from which to find ConfigPoints.  May be null
	 * @param naming  A naming strategy to use when reading the properties during loading
	 * @return A fully configured instance
	 */
	public static RuntimeDefinition 
		doRegisterConfigPoints(List<Class<? extends PropertyGroup>> groups, List<Loader> loaders, NamingStrategy naming) {

		RuntimeDefinition appDef = new RuntimeDefinition();
		
		if (loaders != null) {
			for (Loader loader : loaders) {
				Class<? extends PropertyGroup> group = loader.getLoaderConfig();
				if (group != null) {
					
					doRegisterGroup(appDef, group, naming);
					
				}
			}
		}
		
		//null groups is possible - used in testing and possibly early uses before params are created
		if (groups != null) {
			for (Class<? extends PropertyGroup> group : groups) {

				doRegisterGroup(appDef, group, naming);
				
			}
		}
		
		return appDef;

	}
		
	protected static void doRegisterGroup(RuntimeDefinition appDef,
			Class<? extends PropertyGroup> group, NamingStrategy naming) {

		try {
			List<PropertyGroup.NameAndProperty> nameAndPoints = PropertyGroup.getConfigPoints(group);
			
			for (PropertyGroup.NameAndProperty nameAndPoint : nameAndPoints) {
				NamingStrategy.Naming names = naming.buildNamesFromCanonical(nameAndPoint.property, group, nameAndPoint.canonName);
				appDef.addProperty(group, nameAndPoint.property, names);
			}
			
		} catch (Exception ex) {
			ConstructionProblem.SecurityException se = new ConstructionProblem.SecurityException(
				ex, group);
			appDef.addConstructionProblem(se);
		}

	}
		
	public static void printExceptions(List<? extends Exception> exceptions, PrintStream out) {
		for (Exception ne : exceptions) {
			out.println(ne.getMessage());
		}
	}
	
	public static AppFatalException buildFatalException(ArrayList<RequirementProblem> requirementsProblems,
			ValueMapWithContext loadedValues) {
		
		ArrayList<PropertyValueProblem> pvps = new ArrayList();
		
		//build list of PointValueProblems
		
		if (loadedValues != null) {
			for (LoaderValues lvs : loadedValues.getAllLoaderValues()) {
				for (PropertyValue pv : lvs.getValues()) {
					pvps.addAll(pv.getIssues());
				}
			}
		}
		
		return new AppFatalException(
				"Unable to complete application configuration due to problems. " +
				"See the System.err out or the log files for complete details.",
				pvps, requirementsProblems);
		
	}
}
