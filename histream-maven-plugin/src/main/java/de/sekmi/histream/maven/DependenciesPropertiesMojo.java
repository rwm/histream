package de.sekmi.histream.maven;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo( name = "dependency-properties", requiresDependencyResolution = ResolutionScope.TEST,
defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true )
public class DependenciesPropertiesMojo  extends AbstractMojo{


    /**
     * The current Maven project
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

    /**
     * Skip plugin execution completely.
     *
     * @since 2.7
     */
    @Parameter( property = "mdep.skip", defaultValue = "false" )
    private boolean skip;

    /**
     * Main entry into mojo. Gets the list of dependencies and iterates through setting a property for each artifact.
     *
     * @throws MojoExecutionException with a message if an error occurs.
     */
    public void execute() throws MojoExecutionException
    {
        Set<Artifact> artifacts = project.getArtifacts();
        for ( Artifact artifact : artifacts )
        {
				project.getProperties().setProperty( artifact.getDependencyConflictId()+".version",
				                                     artifact.getVersion() );
        }
    }

}
