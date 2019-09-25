package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSublayoutVerificationMatchRule {

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
     * """Tests a sublayout and checks if a MATCH rule is successful after sublayout
  is resolved into a summary link."""

  def test_verify_sublayout_match_rule(self):

    # Backup original cwd
    working_dir = os.getcwd()

    # Find demo files
    demo_files = os.path.join(
      os.path.dirname(os.path.realpath(__file__)), "demo_files")

    script_files = os.path.join(
      os.path.dirname(os.path.realpath(__file__)), "scripts")

    # Create and change into temporary directory
    test_dir = os.path.realpath(tempfile.mkdtemp())
    os.chdir(test_dir)

    # We don't need to copy the demo files, we just load the keys
    keys = {}
    for key_name in ["alice", "bob"]:
      keys[key_name + "_priv"] = import_rsa_key_from_file(
        os.path.join(demo_files, key_name))
      keys[key_name + "_pub"] = import_rsa_key_from_file(
        os.path.join(demo_files, key_name + ".pub"))

    # Create layout hierarchy

    # Root layout
    # The root layout is the layout that will be passed to `in_toto_verify`
    # It only has one step which is a sublayout, into which verification
    # recurses. Only the root layout and root layout verification key will be
    # passed to verification.
    root_layout_pub_key_dict = {
      keys["alice_pub"]["keyid"]: keys["alice_pub"]
    }

    root_layout_step_name = "delegated-to-bob"

    root_layout = Metablock(signed=Layout(
      keys={
        keys["bob_pub"]["keyid"]: keys["bob_pub"]
      },
      steps=[
        Step(
          name=root_layout_step_name,
          pubkeys=[
            keys["bob_pub"]["keyid"]
          ],
          expected_products=[["MATCH", "foo.tar.gz", "WITH", "PRODUCTS",
              "FROM", root_layout_step_name], ["DISALLOW", "*"]]
        )
      ]
    )
    )
    root_layout.sign(keys["alice_priv"])


    # Sublayout (first level)
    # The sublayout will be treated as a link from the superlayout's
    # perspective and loaded from the current working directory. The links for
    # the steps of this sublayout will be placed in a namespaced subdir.
    bobs_layout_name = FILENAME_FORMAT.format(
      step_name=root_layout_step_name,
      keyid=keys["bob_pub"]["keyid"])

    bobs_layout_link_dir = SUBLAYOUT_LINK_DIR_FORMAT.format(
      name=root_layout_step_name,
      keyid=keys["bob_pub"]["keyid"])
    os.mkdir(bobs_layout_link_dir)

    bobs_layout = Metablock.load(os.path.join(demo_files, "demo.layout.template"))
    bobs_layout.sign(keys["bob_priv"])
    bobs_layout.dump(bobs_layout_name)
    shutil.copy2(os.path.join(demo_files, "write-code.776a00e2.link"), bobs_layout_link_dir)
    shutil.copy2(os.path.join(demo_files, "package.2f89b927.link"), bobs_layout_link_dir)
    shutil.copy2(os.path.join(demo_files, "foo.tar.gz"), ".")
    shutil.copytree(script_files, os.path.join(".", "scripts"))

    in_toto_verify(root_layout, root_layout_pub_key_dict)

    os.chdir(working_dir)
    shutil.rmtree(test_dir)
     */

}
