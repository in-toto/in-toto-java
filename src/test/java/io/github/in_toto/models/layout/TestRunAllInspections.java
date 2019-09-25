package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.layout.rule.AllowRule;
import io.github.in_toto.models.layout.rule.CreateRule;
import io.github.in_toto.models.layout.rule.DeleteRule;
import io.github.in_toto.models.layout.rule.DisAllowRule;
import io.github.in_toto.models.layout.rule.MatchRule;
import io.github.in_toto.models.layout.rule.ModifyRule;
import io.github.in_toto.models.layout.rule.RequireRule;
import io.github.in_toto.models.layout.rule.Rule;
import io.github.in_toto.models.layout.rule.MatchRule.DestinationType;
import io.github.in_toto.models.link.Artifact;

class TestRunAllInspections {
    
    static String sha1 = "d65165279105ca6773180500688df4bdc69a2c7b771752f0a46ef120b7fd8ec3";
    static String sha2 = "155c693a6b7481f48626ebfc545f05236df679f0099225d6d0bc472e6dd21155";
    static Artifact foo = new Artifact("foo", sha1);
    static Artifact foobar = new Artifact("foobar", sha1);
    static Artifact bar1 = new Artifact("bar", sha1);
    static Artifact bar2 = new Artifact("bar", sha2);
    static Artifact baz = new Artifact("baz", sha1);
    static Artifact foobarbaz = new Artifact("foobarbaz", sha1);
    static RSAKey alice = RSAKey.read("src/test/resources/demo_files/alice");
    static RSAKey bob = RSAKey.read("src/test/resources/demo_files/bob");
    static Metablock<Layout> layoutMetablock = null;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        List<Rule> expectedMaterials = Arrays.asList(
                new DeleteRule("foobar"),
                new RequireRule("foobarbaz"),
                new CreateRule("baz"),
                new ModifyRule("bar"),
                new MatchRule("foo", null, null, DestinationType.MATERIALS, new Step("item")), // match with self
                new AllowRule("foobarbaz"),
                new DisAllowRule("*"));
        List<Rule> expectedProducts = Arrays.asList(
                new AllowRule("foo"),
                new AllowRule("bar"),
                new DisAllowRule("*"));
        Inspection inspection = new Inspection("touch-bar", expectedMaterials, expectedProducts, Arrays.asList("python", "bla/touch", "bar"));
        Set<Inspection> inspections = new HashSet<>(Arrays.asList(inspection));
        Layout layout = new Layout("root", null, inspections, new HashSet<>(Arrays.asList(alice, bob)), null, null);
        layoutMetablock = new Metablock<>(layout, null);
        layoutMetablock.sign(alice);
    }

    /*
     *   @classmethod
  def setUpClass(self):
    """
    Create layout with dummy inpsection.
    Create and change into temp test directory with dummy artifact."""

    # find where the scripts directory is located.
    scripts_directory = os.path.join(
        os.path.dirname(os.path.realpath(__file__)), "scripts")

    # Create layout with one inspection
    self.layout = Layout.read({
        "_type": "layout",
        "steps": [],
        "inspect": [{
          "name": "touch-bar",
          "run": ["python", os.path.join(scripts_directory, "touch"), "bar"],
        }]
      })

    # Create directory where the verification will take place
    self.working_dir = os.getcwd()
    self.test_dir = os.path.realpath(tempfile.mkdtemp())
    os.chdir(self.test_dir)
    open("foo", "w").write("foo")

  @classmethod
  def tearDownClass(self):
    """Change back to initial working dir and remove temp test directory. """
    os.chdir(self.working_dir)
    shutil.rmtree(self.test_dir)

  def test_inpsection_artifacts_with_base_path_ignored(self):
    """Create new dummy test dir and set as base path, must ignore. """
    ignore_dir = os.path.realpath(tempfile.mkdtemp())
    ignore_foo = os.path.join(ignore_dir, "ignore_foo")
    open(ignore_foo, "w").write("ignore foo")
    in_toto.settings.ARTIFACT_BASE_PATH = ignore_dir

    run_all_inspections(self.layout)
    link = Metablock.load("touch-bar.link")
    self.assertListEqual(list(link.signed.materials.keys()), ["foo"])
    self.assertListEqual(sorted(list(link.signed.products.keys())), sorted(["foo", "bar"]))

    in_toto.settings.ARTIFACT_BASE_PATH = None
    shutil.rmtree(ignore_dir)

  def test_inspection_fail_with_non_zero_retval(self):
    """Test fail run inspections with non-zero return value. """
    layout = Layout.read({
        "_type": "layout",
        "steps": [],
        "inspect": [{
          "name": "non-zero-inspection",
          "run": ["python", "./scripts/expr", "1", "/", "0"],
        }]
    })
    with self.assertRaises(BadReturnValueError):
      run_all_inspections(layout)
     */

}
