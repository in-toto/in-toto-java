package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestInTotoVerifyMultiLevelSublayouts {

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
     * def test_verify_multi_level_sublayout(self):
    # Backup original cwd
    working_dir = os.getcwd()

    # Find demo files
    demo_files = os.path.join(
        os.path.dirname(os.path.realpath(__file__)), "demo_files")

    # Create and change into temporary directory
    test_dir = os.path.realpath(tempfile.mkdtemp())
    os.chdir(test_dir)

    # We don't need to copy the demo files, we just load the keys
    keys = {}
    for key_name in ["alice", "bob", "carl"]:
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

    root_layout_name = "root.layout"
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
              ]
            )
          ]
        )
      )
    root_layout.sign(keys["alice_priv"])


    # Sublayout (first level)
    # The first level sublayout wil be treated as a link from the
    # superlayout's perspective and loaded from the current working directory.
    # The link for the only step of this sublayout will be placed in a
    # namespaced subdir, that link itself is a sublayout (subsublayout).
    bobs_layout_name = FILENAME_FORMAT.format(
        step_name=root_layout_step_name,
        keyid=keys["bob_pub"]["keyid"])

    bobs_layout_link_dir = SUBLAYOUT_LINK_DIR_FORMAT.format(
        name=root_layout_step_name,
        keyid=keys["bob_pub"]["keyid"])
    os.mkdir(bobs_layout_link_dir)

    bobs_layout_step_name = "delegated-to-carl"

    bobs_layout = Metablock(signed=Layout(
        keys={
          keys["carl_pub"]["keyid"]: keys["carl_pub"]
          },
        steps=[
            Step(
              name=bobs_layout_step_name,
              pubkeys=[keys["carl_pub"]["keyid"]]
            )
          ]
        )
      )
    bobs_layout.sign(keys["bob_priv"])
    bobs_layout.dump(bobs_layout_name)


    # Subsublayout (second level)
    # The subsublayout will be placed in the namespaced link dir
    # of its superlayout (sublayout from the root layout's perspective), for
    # for which it serves as link.
    carls_layout_name = FILENAME_FORMAT.format(
            step_name=bobs_layout_step_name,
            keyid=keys["carl_pub"]["keyid"])

    carls_layout_path = os.path.join(bobs_layout_link_dir, carls_layout_name)
    carls_layout = Metablock(signed=Layout())
    carls_layout.sign(keys["carl_priv"])
    carls_layout.dump(carls_layout_path)

    in_toto_verify(root_layout, root_layout_pub_key_dict)

    os.chdir(working_dir)
    shutil.rmtree(test_dir)
     */

}
