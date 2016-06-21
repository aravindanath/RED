package org.rf.ide.core.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;

@SuppressWarnings("PMD.MethodNamingConventions")
public class RobotParserTest {

    @Test
    public void test_create_when_robotFramework_correct29() {
        // prepare
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        RobotParser parser = RobotParser.create(projectHolder);

        // verify
        RobotVersion robotVersion = parser.getRobotVersion();
        assertThat(robotVersion).isNotNull();
        assertThat(robotVersion.isEqualTo(new RobotVersion(2, 9))).isTrue();
    }

    @Test
    public void test_create_when_robotFramework_isNotPresent() {
        // prepare
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn(null);
        RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        // execute
        RobotParser parser = RobotParser.create(projectHolder);

        // verify
        assertThat(parser.getRobotVersion()).isNull();
    }

    @Test(timeout = 10000)
    public void test_loopedResources_shouldPassFastAndWithoutAny_reReadFiles_BUG_RED_352_GITHUB_23() throws Exception {
        // prepare
        RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        RobotProjectHolder projectHolder = spy(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);

        RobotParser parser = spy(RobotParser.createEager(projectHolder));

        //// prepare paths
        final String mainPath = "parser/bugs/RED_352_ReadManyTimesPrevReadReferenceFile_LoopPrevent/";
        final File startFile = new File(this.getClass().getResource(mainPath + "StartFile.robot").toURI());
        final File normalFile = new File(this.getClass().getResource(mainPath + "NormalFile.robot").toURI());
        final File anotherLoop = new File(this.getClass().getResource(mainPath + "anotherLoop.robot").toURI());
        final File loopEndWithRefToFirst = new File(
                this.getClass().getResource(mainPath + "resources/loopEndWithRefToFirst.robot").toURI());
        final File middle = new File(this.getClass().getResource(mainPath + "resources/Middle.robot").toURI());
        final File theFirst = new File(this.getClass().getResource(mainPath + "resources/theFirst.robot").toURI());

        // execute
        List<RobotFileOutput> output = parser.parse(startFile);

        // verify content
        //// StartFile.robot
        assertThat(output).hasSize(1);
        RobotFileOutput startFileOutput = output.get(0);
        assertThat(startFileOutput.getProcessedFile()).isEqualTo(startFile);

        List<ResourceImportReference> resourceImportReferences = startFileOutput.getResourceImportReferences();
        assertThat(resourceImportReferences).hasSize(3);

        ResourceImportReference theFirstImportMain = resourceImportReferences.get(0);
        assertThat(theFirstImportMain.getImportDeclaration().getPathOrName().getText()).isEqualTo("NormalFile.robot");
        assertThat(theFirstImportMain.getReference().getProcessedFile()).isEqualTo(normalFile);

        ResourceImportReference anotherFileResource = resourceImportReferences.get(1);
        assertThat(anotherFileResource.getImportDeclaration().getPathOrName().getText()).isEqualTo("anotherLoop.robot");
        assertThat(anotherFileResource.getReference().getProcessedFile()).isEqualTo(anotherLoop);

        ResourceImportReference res_theFirst = resourceImportReferences.get(2);
        assertThat(res_theFirst.getImportDeclaration().getPathOrName().getText()).isEqualTo("resources/theFirst.robot");
        assertThat(res_theFirst.getReference().getProcessedFile()).isEqualTo(theFirst);

        //// NormalFile.robot
        RobotFileOutput normalFileOutput = theFirstImportMain.getReference();
        assertThat(normalFileOutput.getResourceImportReferences()).hasSize(0);

        //// anotherLoop.robot
        RobotFileOutput anotherFileOutput = anotherFileResource.getReference();
        final List<ResourceImportReference> anotherLoopRefs = anotherFileOutput.getResourceImportReferences();
        assertThat(anotherLoopRefs).hasSize(1);

        ResourceImportReference loopEndRef = anotherLoopRefs.get(0);
        assertThat(loopEndRef.getImportDeclaration().getPathOrName().getText())
                .isEqualTo("resources/loopEndWithRefToFirst.robot");
        assertThat(loopEndRef.getReference().getProcessedFile()).isEqualTo(loopEndWithRefToFirst);

        //// loopEndWithRefToFirst.robot
        RobotFileOutput loopEndOutput = loopEndRef.getReference();
        List<ResourceImportReference> loopEndRefs = loopEndOutput.getResourceImportReferences();
        assertThat(loopEndRefs).hasSize(1);

        ResourceImportReference middleRef = loopEndRefs.get(0);
        assertThat(middleRef.getImportDeclaration().getPathOrName().getText()).isEqualTo("../resources/Middle.robot");
        assertThat(middleRef.getReference().getProcessedFile()).isEqualTo(middle);

        //// middle.robot
        RobotFileOutput middleOutput = middleRef.getReference();
        List<ResourceImportReference> middleRefs = middleOutput.getResourceImportReferences();
        assertThat(middleRefs).hasSize(1);

        ResourceImportReference res_theFirstAgain = middleRefs.get(0);
        assertThat(res_theFirstAgain.getImportDeclaration().getPathOrName().getText())
                .isEqualTo("../resources/theFirst.robot");
        assertThat(res_theFirstAgain.getReference()).isSameAs(res_theFirst.getReference());

        // verify order
        InOrder order = inOrder(projectHolder, parser);
        order.verify(projectHolder, times(1)).shouldBeLoaded(startFile);
        order.verify(projectHolder, times(1)).addModelFile(output.get(0));
        order.verify(projectHolder, times(1)).shouldBeLoaded(normalFile);
        order.verify(projectHolder, times(1)).addModelFile(theFirstImportMain.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(anotherLoop);
        order.verify(projectHolder, times(1)).addModelFile(anotherFileResource.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(loopEndWithRefToFirst);
        order.verify(projectHolder, times(1)).addModelFile(loopEndRef.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(middle);
        order.verify(projectHolder, times(1)).addModelFile(middleRef.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(theFirst);
        order.verify(projectHolder, times(1)).addModelFile(res_theFirst.getReference());
        order.verify(projectHolder, times(1)).shouldBeLoaded(theFirst);
        order.verify(projectHolder, times(1)).findFileByName(theFirst);

    }
}
