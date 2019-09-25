package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestVerifySublayouts {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {
     // TODO
    }
    
    /*
     * """Tests verifylib.verify_sublayouts(layout, reduced_chain_link_dict).
  Call with one-step super layout that has a sublayout (demo layout). """

  @classmethod
  def setUpClass(self):
    """Creates and changes into temporary directory and prepares two layouts.
    The superlayout, which has one step and its sublayout, which is the usual
    demo layout (write code, package, inspect tar). """

    # Backup original cwd
    self.working_dir = os.getcwd()

    # Find demo files
    demo_files = os.path.join(
        os.path.dirname(os.path.realpath(__file__)), "demo_files")

    # find where the scripts directory is located.
    scripts_directory = os.path.join(
        os.path.dirname(os.path.realpath(__file__)), "scripts")

    # Create and change into temporary directory
    self.test_dir = os.path.realpath(tempfile.mkdtemp())
    os.chdir(self.test_dir)

    # Copy demo files to temp dir
    for file in os.listdir(demo_files):
      shutil.copy(os.path.join(demo_files, file), self.test_dir)

    # copy portable scripts over
    shutil.copytree(scripts_directory, 'scripts')

    # Import sub layout signing (private) and verifying (public) keys
    alice = import_rsa_key_from_file("alice")
    alice_pub = import_rsa_key_from_file("alice.pub")

    # From the perspective of the superlayout, the sublayout is treated as
    # a link corresponding to a step, hence needs a name.
    sub_layout_name = "sub_layout"

    # Sublayout links are expected in a directory relative to the superlayout's
    # link directory
    sub_layout_link_dir = SUBLAYOUT_LINK_DIR_FORMAT.format(
        name=sub_layout_name, keyid=alice["keyid"])

    for sublayout_link_name in glob.glob("*.link"):
      dest_path = os.path.join(sub_layout_link_dir, sublayout_link_name)
      os.renames(sublayout_link_name, dest_path)


    # Copy, sign and dump sub layout as link from template
    layout_template = Metablock.load("demo.layout.template")
    sub_layout = copy.deepcopy(layout_template)
    sub_layout_path = FILENAME_FORMAT.format(step_name=sub_layout_name,
        keyid=alice_pub["keyid"])
    sub_layout.sign(alice)
    sub_layout.dump(sub_layout_path)

    # Create super layout that has only one step, the sublayout
    self.super_layout = Layout()
    self.super_layout.keys[alice_pub["keyid"]] = alice_pub
    sub_layout_step = Step(
        name=sub_layout_name,
        pubkeys=[alice_pub["keyid"]]
      )
    self.super_layout.steps.append(sub_layout_step)

    # Load the super layout links (i.e. the sublayout)
    self.super_layout_links = load_links_for_layout(self.super_layout, ".")

  @classmethod
  def tearDownClass(self):
    """Change back to initial working dir and remove temp dir. """
    os.chdir(self.working_dir)
    shutil.rmtree(self.test_dir)

  def test_verify_demo_as_sublayout(self):
    """Test super layout's passing sublayout verification. """
    verify_sublayouts(
        self.super_layout, self.super_layout_links, ".")

     */

}
