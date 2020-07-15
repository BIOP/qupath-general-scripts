// Short Script to import relative projects from QuPath 0.1.4
// A Method using File > Project > Import images from v0.1.2 is available here
// 

def qupath = getQuPath()
def project = qupath.getProject()
def title = "Import legacy project with relative paths"
if (project == null) {
    Dialogs.showNoProjectError( title )
    return
}
		
// Prompt for the old project
def project_file = Dialogs.promptForFile(title, null, "Project (v0.1.4)", ".qpproj")
if (project_file == null) return false
		    

// Read the entries
def project_directory = project_file.getParent()
println( "Getting Project from &project_directory" )

def reader = new FileReader( project_file )
def oldProject = GsonTools.getInstance().fromJson( reader, ProjectCommands.LegacyProject.class )

// Replace any instance of {$PROJECT_DIR} with the current project folder
oldProject.getEntries().each{ entry ->
    entry.path = entry.path.replace("{\$PROJECT_DIR}", project_directory)
}

println( "Importing the following Images:" )

oldProject.getEntries().each{ println("    "+it) }

def dirData = new File( project_file.getParent(), "data" )
if ( !dirData.exists() ) {
    Dialogs.showErrorMessage(title, "No data directory found for the legacy project!")
    return
}
		
def task = new ProjectCommands.LegacyProjectTask(project, oldProject.getEntries(), project_file.getParentFile())
Platform.runLater( task )
def nImages = oldProject.getEntries().size()

try {
    project.syncChanges()
    //In case the user makes a mistake, make sure that each image has a unique name
    project.images = project.images.toUnique{ it.getImageName() }
} catch (def e) {
    logger.error("Error syncing project: " + e.getLocalizedMessage(), e)
}
    qupath.refreshProject()
    
return




import qupath.lib.gui.commands.*
import qupath.lib.gui.dialogs.Dialogs
