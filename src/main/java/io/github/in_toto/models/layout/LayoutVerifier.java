package io.github.in_toto.models.layout;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.in_toto.exceptions.CommandVerifyError;
import io.github.in_toto.exceptions.FormatError;
import io.github.in_toto.exceptions.InspectionExecutionError;
import io.github.in_toto.exceptions.LayoutExpiredError;
import io.github.in_toto.exceptions.LayoutVerificationError;
import io.github.in_toto.exceptions.RuleVerificationError;
import io.github.in_toto.exceptions.SignatureVerificationError;
import io.github.in_toto.exceptions.StepEqualLinksVerificationError;
import io.github.in_toto.exceptions.ThresholdVerificationError;
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.Signature;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.link.ByProducts;
import io.github.in_toto.models.link.Link;
import io.github.in_toto.models.link.Link.LinkBuilder;

public final class LayoutVerifier {
    private static final Logger logger = Logger.getLogger(LayoutVerifier.class.getName());
    
    private LayoutVerifier() {}
    
    /**
     * verifies all steps and runs the inspections
     * 
     * @param layoutMetablock Metablock Layout object that is being verified.
     * @param links link metadata objects corresponding to the steps in the passed layout are loaded.
     * @param step The step that the layout corresponds to, typically used during
            recursive calls of in_toto_verify. This usually happens when
            resolving sublayouts.
     * @param runInspections If true run and verify also all inspections
     * @throws FormatError 
     * @throws LayoutVerificationError 
     */
    /**
     * 
     * @param layoutMetablock
     * @param links
     * @param step
     * @param runInspections
     * @throws LayoutVerificationError
     */
    public static void verify(Metablock<Layout> layoutMetablock, List<Metablock<Link>> links, Step step, boolean runInspections) throws LayoutVerificationError {
        Map<String, List<Metablock<Link>>> stepMap = verifySteps(layoutMetablock, links, step);
        if (runInspections) {
            runInspections(layoutMetablock.getSigned(), stepMap);
        }
    }
    
    /**
     * Does entire in-toto supply chain verification of steps in a layout.
     * 
     * performing the following actions:

        1.  Verify layout signature(s), requires at least one verification key
            to be passed, and a valid signature for each passed key.

        2.  Verify layout expiration


        3.  Load link metadata for every Step defined in the layout and
            fail if less links than the defined threshold for a step are found.
            NOTE: Link files are expected to have the corresponding step
            and the functionary, who carried out the step, encoded in their
            filename.

        4.  Verify functionary signature for every loaded Link, skipping links
            with failing signatures or signed by unauthorized functionaries,
            and fail if less than `threshold` links validly signed by different
            authorized functionaries can be found.
            The routine returns a dictionary containing only links with valid
            signatures by authorized functionaries.

        5.  Verify sublayouts
            Recurses into layout verification for each link of the
            superlayout that is a layout itself (i.e. sublayout).
            Links for the sublayout are expected to be in a subdirectory
            relative to the superlayout's link_dir_path, with a name in the
            format: in_toto.models.layout.SUBLAYOUT_LINK_DIR_FORMAT.

            The successfully verified sublayout is replaced with an unsigned
            summary link in the chain_link_dict of the superlayout.
            The summary link is then used just like a regular link
            to verify command alignments, thresholds and inspections according
            to the superlayout.

        6.  Verify alignment of defined (Step) and reported (Link) commands
            NOTE: Won't raise exception on mismatch

        7.  Verify threshold constraints, i.e. if all links corresponding to
            one step have recorded the same artifacts (materials and products).

        8.  Verify rules defined in each Step's expected_materials and
            expected_products field
            NOTE: At this point no Inspection link metadata is available,
            hence (MATCH) rules cannot reference materials or products of
            Inspections.
            Verifying Steps' artifact rules before executing Inspections
            guarantees that Inspection commands don't run on compromised
            target files, which would be a surface for attacks.

        9.  Execute Inspection commands 
            NOTE: Inspections, similar to Steps executed with 'in-toto-run',
            will record materials before and products after command execution.
            For now it records everything in the current working directory.

        10. Verify rules defined in each Inspection's expected_materials and
            expected_products field 
            
     * @param layoutMetablock Metablock Layout object that is being verified.
     * @param links link metadata objects corresponding to the steps in the passed layout are loaded.
     * @param step The step that the layout corresponds to, typically used during
            recursive calls of in_toto_verify. This usually happens when
            resolving sublayouts.
     * @throws FormatError 
     * @throws LayoutVerificationError 
     */
    private static Map<String, List<Metablock<Link>>> verifySteps(Metablock<Layout> layoutMetablock, List<Metablock<Link>> links, Step step) throws LayoutVerificationError {
        /*
         * , requires at least one verification key
            to be passed, and a valid signature for each passed key.
         */
        verifyLayoutSignatures(layoutMetablock);
        
        
        verifyExpirationDate(layoutMetablock.getSigned());
        
        /*
         * Verify functionary signature for every loaded Link, skipping links
            with failing signatures or signed by unauthorized functionaries.
            The routine returns a dictionary containing only links with valid
            signatures by authorized functionaries.
         */
        List<Metablock<Link>> prunedLinks = verifySignatureOnLinks(layoutMetablock, links);
        
        /*
         * Check every Step defined in the layout and
            fail if less links than the defined threshold for a step are found.
         */
        Map<String, List<Metablock<Link>>> stepMap = LayoutVerifier.verifyLinkSignatureThresholds(layoutMetablock.getSigned(), prunedLinks);
        
        /*
         * Verify sublayouts
            Recurses into layout verification for each link of the
            superlayout that is a layout itself (i.e. sublayout).
            Links for the sublayout are expected to be in a subdirectory
            relative to the superlayout's link_dir_path, with a name in the
            format: in_toto.models.layout.SUBLAYOUT_LINK_DIR_FORMAT.

            The successfully verified sublayout is replaced with an unsigned
            summary link in the chain_link_dict of the superlayout.
            The summary link is then used just like a regular link
            to verify command alignments, thresholds and inspections according
            to the superlayout.
         */
        verifySubLayout(layoutMetablock, links, step);
        /*
         * Verify alignment of defined (Step) and reported (Link) commands
         */
        for (Step tmpStep:layoutMetablock.getSigned().getSteps()) {
            for (Metablock<Link> link:stepMap.get(tmpStep.getName())) {
                LayoutVerifier.verifyCommandAlignment(tmpStep, link.getSigned());
            }
        }
        
        /*
         * Verify threshold constraints, i.e. if all links corresponding to
            one step have recorded the same artifacts (materials and products).
         */
        LayoutVerifier.verifyStepLinkConstraints(layoutMetablock.getSigned(), stepMap);
        
        /*
         * Verify rules defined in each Step's expected_materials and
            expected_products field
         */
        verifyAllItemRules(layoutMetablock.getSigned().getSteps(), stepMap);
        
        return stepMap;
        
    }
    /**
     * Verify layout signature(s)
     * 
     * Iteratively verifies the signatures of a Metablock object containing a Layout
     * object for every verification key in the signers.
     * 
     * Requires at least one key as signer and requires every key in signers to find
     * a valid signature.
     * 
     * @param layoutMetablock A Metablock object containing a Layout whose
     *                        signatures are verified.
     * @throws SignatureVerificationError if any of the keys fails to verify a
     *                                    signature.
     * @throws FormatError                if the signing key does not match
     *                                    ANY_VERIFICATION_KEY_DICT_SCHEMA
     */
    private static void verifyLayoutSignatures(Metablock<Layout> layoutMetablock) throws SignatureVerificationError{
        Layout layout = layoutMetablock.getSigned();
        // get signing keys from keys in layout
        List<Key> keyList = new ArrayList<>();
        if (layout.getKeys() == null || layout.getKeys().isEmpty()) {
            throw new SignatureVerificationError("No keys available for verification.");
        }
        Iterator<Key> keys = layout.getKeys().iterator();
        while (keys.hasNext()) {
            keyList.add(keys.next());
        }
        /*
         * Fail if any of the passed keys can't verify a signature on the Layout
         * or signingKeys doesn't contain all necessary keys
          */
        layoutMetablock.verifySignatures(keyList);
    }
   
    /**
     *  Verify layout expiration
     * Returns true if the passed layout has expired, i.e. if its
    `expires` property is lesser "now".
    Time zone aware datetime objects in UTC+00:00 (Zulu Time) are used.
     * @param layoutMetablock The Layout object to be verified.
     * @return boolean true if expired
     * @throws LayoutExpiredError 
     * @throws FormatError
     */
    private static void verifyExpirationDate(Layout layout) throws LayoutExpiredError {
        if (layout.isExpired()) {
            throw new LayoutExpiredError(layout);
        }
    }
    
    private static List<Metablock<Link>> verifySignatureOnLinks(Metablock<Layout> layoutMetablock, List<Metablock<Link>> links) {
        List<Metablock<Link>> prunedLinks = new ArrayList<>();
        Layout layout = layoutMetablock.getSigned();
        // get signing keys from keys in layout
        List<Key> keyList = new ArrayList<>();
        Iterator<Key> keys = null;
        if (layout.getKeys() != null) {
            keys = layout.getKeys().iterator();
            while (keys.hasNext()) {
                keyList.add(keys.next());
            }
        }
        for (Metablock<Link> link:links) {
            try {
                link.verifySignatures(keyList);
                prunedLinks.add(link);
            } catch (SignatureVerificationError e) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("On verifying layout [%s] skip link [%s] with signature verification error [%s] .", 
                        layoutMetablock.getSigned().getName(), link.getFullName(), e.getMessage()));
                }
            }
        }
        return prunedLinks;
    }
    
    /**
     * Check if all links are available
     * 
     * Verify that for each step of the layout there are at least `threshold`
    links, signed by different authorized functionaries and return the chain
    link dictionary containing only authorized links whose signatures
    were successfully verified.

    NOTE: If the layout's key store (`layout.keys`) lists a (master) key `K`,
    with a subkey `K'`, then `K'` is authorized implicitly, to sign any link
    that `K` is authorized to sign. In other words, the trust in a master key
    extends to the trust in a subkey. The inverse is not true.
    
     * @param layout A Layout object whose Steps are extracted and verified.
     * @param links
     * @return  A list containing only links with valid signatures created by authorized functionaries.
     * @throws ThresholdVerificationError If any of the steps of the passed layout does not have enough
            (`step.threshold`) links signed by different authorized
            functionaries.
     * @throws SignatureVerificationError 
     */
    private static Map<String, List<Metablock<Link>>> verifyLinkSignatureThresholds(Layout layout, List<Metablock<Link>> links) throws ThresholdVerificationError {
        //throw new UnsupportedOperationException("Not implemented.");
  /* ==> not yet implemented
  # Create an inverse keys-subkeys dictionary, with subkey keyids as
  # dictionary keys and main keys as dictionary values. This will be
  # required below to assess main-subkey trust delegations.
  # We assume that a given subkey can only belong to one master key
  # TODO: Is this a safe assumption? Should we assert for it?
  main_keys_for_subkeys = {}
  for main_key in list(layout.keys.values()):
    for sub_keyid in main_key.get("subkeys", []):
      main_keys_for_subkeys[sub_keyid] = main_key
*/
        /*
  # Dict for valid and authorized links of all steps of the layout
  verfied_chain_link_dict = {}

  # For each step of the layout check the signatures of corresponding links.
  # Consider only links where the signature is valid and keys are authorized,
  # and discard others.
  # Only count one of multiple links signed with different subkeys of a main
  # key towards link threshold.
  # Only proceed with final product verification if threshold requirements are
  # fulfilled.
  */
        Map<String, List<Metablock<Link>>> stepMap = new HashMap<>();
        Map<String, Key> keyMap = new HashMap<>();
        for (Key key: layout.getKeys()) {
            keyMap.put(key.getKeyid(), key);
        }
        
        Map<String, List<Metablock<Link>>> linkMap = new HashMap<>();
        for (Metablock<Link> link:links) {
            linkMap.putIfAbsent(link.getSigned().getName(), new ArrayList<Metablock<Link>>());
            linkMap.get(link.getSigned().getName()).add(link);
        }
        
        for (Step step:layout.getSteps()) {
            stepMap.putIfAbsent(step.getName(), new ArrayList<Metablock<Link>>());
            List<Metablock<Link>> stepLinks = new ArrayList<>();
            if (linkMap.containsKey(step.getName())) {
                stepLinks = linkMap.get(step.getName());
            }
            List<Key> usedKeys = new ArrayList<>();
            for (Metablock<Link> link:stepLinks) {
                for (Signature sig: link.getSignatures()) {
                    if (step.getAuthorizedKeys().contains(sig.getKey())
                            && keyMap.containsKey(sig.getKey().getKeyid())) {
                        usedKeys.add(keyMap.get(sig.getKey().getKeyid()));
                    } 
                }
                try {
                    link.verifySignatures(usedKeys);
                    stepMap.get(step.getName()).add(link);
                    
                } catch (SignatureVerificationError e) {
                    logger.info(e.getMessage());
                }
            }
            if (stepMap.get(step.getName()).size() < step.getThreshold()) {
                throw new ThresholdVerificationError(step, stepMap.get(step.getName()).size());
            }
        }
        return stepMap;
    }
    
    /**
     * """
  <Purpose>
    Checks if any step has been delegated by the functionary, recurses into
    the delegation and replaces the layout object in the chain_link_dict
    by an equivalent link object.

  <Arguments>
    layout:
            The layout specified by the project owner.

    chain_link_dict:
            A dictionary containing link metadata per functionary per step,
            e.g.:
            {
              <link name> : {
                <functionary key id> : <Metablock containing a Link or Layout
                                          object>,
                ...
              }, ...
            }

    superlayout_link_dir_path:
            A path to a directory, where links of the superlayout are loaded
            from. Links of the sublayout are expected to be in a subdirectory
            relative to this path, with a name in the format
            in_toto.models.layout.SUBLAYOUT_LINK_DIR_FORMAT.

  <Exceptions>
    raises an Exception if verification of the delegated step fails.

  <Side Effects>
    None.

  <Returns>
    The passed dictionary containing link metadata per functionary per step,
    with layouts replaced with summary links.
    e.g.:
    {
      <link name> : {
        <functionary key id> : <Metablock containing a Link object>,
        ...
      }, ...
    }

  """
     * @param layoutMetablock The layout specified by the project owner.
     * @param links
     * @param step
     */
    /**
     * 
     * @param layoutMetablock
     * @param links
     * @param step
     * @throws LayoutVerificationError
     */
    private static void verifySubLayout(Metablock<Layout> layoutMetablock, List<Metablock<Link>> links, Step step) throws LayoutVerificationError {
        // TODO
    }
    
    /**
     * Verify alignment of defined (Step) and reported (Link) commands
     * @throws CommandVerifyError 
     */
    private static void verifyCommandAlignment(Step step, Link link) throws CommandVerifyError {
        if (!step.getExpectedCommand().equals(link.getCommand())) {
            throw new CommandVerifyError(String.format("Actual command [%s] != expected command [%s].",
                    link.getCommand(), step.getExpectedCommand()));
        }
    }
    
    /**
     * Verifies that all links corresponding to a given step report the same
     * materials and products.
     * 
     * NOTE: This function does not verify if the signatures of each link
     * corresponding to a step are valid or created by a different authorized
     * functionary. This should be done earlier, using the function
     * `verifyLinkSignatureThresholds`.
     * 
     * ThresholdVerificationError If there are not enough (threshold) links for a
     * steps
     * 
     * If the artifacts for all links of a step are not equal
     * 
     * @param layout  The layout whose step thresholds are being verified
     * @param stepMap A dictionary containing link metadata per functionary per
     *                step, e.g.: { <link name> : { <functionary key id> :
     *                <Metablock containing a Link object>, ... }, ... }
     * @throws StepEqualLinksVerificationError
     */
    private static void verifyStepLinkConstraints(Layout layout, Map<String, List<Metablock<Link>>> stepMap)
            throws StepEqualLinksVerificationError {
        // We are only interested in links that are related to steps defined in the
        // Layout, so iterate over layout.steps
        Iterator<Step> stepIterator = layout.getSteps().iterator();
        while (stepIterator.hasNext()) {
            Step step = stepIterator.next();
            // Skip steps that don't require multiple functionaries
            if (step.getThreshold() <= 1) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("Skipping threshold verification for step [%s] with threshold [%s].",
                        step.getName(), step.getThreshold()));
                }
                continue;
            }
            if (logger.isLoggable(Level.INFO)) {
                logger.info(
                    String.format("Verifying equal materials and products for links for step [%s].", step.getName()));
            }
            // Extract the key_link_dict for this step from the passed chain_link_dict
            List<Metablock<Link>> stepLinkList = stepMap.get(step.getName());

            // Take a reference link (e.g. the first in the step_link_dict)
            Metablock<Link> referenceLinkMetablock = stepMap.get(step.getName()).get(0);
            Link referenceLink = referenceLinkMetablock.getSigned();

            // Iterate over all links to compare their properties with a reference_link
            for (Metablock<Link> linkMetablock : stepLinkList) {
                Link link = linkMetablock.getSigned();
                if (!(referenceLink.equals(link))) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Links [%s] and [%s] are not equal.", referenceLinkMetablock.getFullName(), linkMetablock.getFullName()));
                    }
                    throw new StepEqualLinksVerificationError(referenceLinkMetablock, linkMetablock);
                }
            }
        }

    }
    
    private static <S extends SupplyChainItem> void verifyAllItemRules(Set<S> items, Map<String, List<Metablock<Link>>> stepMap) throws RuleVerificationError {
        if (items != null && !items.isEmpty()) {
            Iterator<S> itemIterator = items.iterator();
            while (itemIterator.hasNext()) {
                SupplyChainItem item = itemIterator.next();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("Verifying rules for [%s]...", item.getName()));
                }
                item.verifyRules(stepMap);
            }
        }
        
    }
    
    /**
     * Does entire in-toto supply chain verification by running the inspections in a layout.
     * 
     * performing the following actions:

        1.  Execute Inspection commands 
            NOTE: Inspections, similar to Steps executed with 'in-toto-run',
            will record materials before and products after command execution.
            For now it records everything in the current working directory.

        2. Verify rules defined in each Inspection's expected_materials and
            expected_products field 
            
     * @param layoutMetablock Metablock Layout object that is being verified.
     * @param links link metadata objects corresponding to the steps in the passed layout are loaded.
     * @throws RuleVerificationError 
     * @throws InspectionExecutionError 
     * @throws FormatError 
     * @throws LayoutVerificationError 
     */
    private static void runInspections(Layout layout, Map<String, List<Metablock<Link>>> stepMap)
            throws RuleVerificationError, InspectionExecutionError {
        Map<String, List<Metablock<Link>>> inspectionMap = runAllInspections(layout);
        for (Entry<String, List<Metablock<Link>>> entry : stepMap.entrySet()) {
            inspectionMap.put(entry.getKey(), entry.getValue());
        }
        verifyAllItemRules(layout.getInspections(), inspectionMap);

    }
    
    /**
     * <Purpose>
    Extracts all inspections from a passed Layout's inspect field and
    iteratively runs each command defined in the Inspection's `run` field using
    `runlib.in_toto_run`, which returns a Metablock object containing a Link
    object.

    If a link command returns non-zero the verification is aborted.

  <Arguments>
    layout:
            A Layout object which is used to extract the Inspections.

  <Exceptions>
    Calls function that raises BadReturnValueError if an inspection returned
    non-int or non-zero.

  <Returns>
    A dictionary of metadata about the executed inspections, e.g.:

    {
      <inspection name> : {
        <Metablock containing a Link object>,
        ...
      }, ...
    }

     * @param layout
     * @throws InspectionExecutionError 
     */
    private static Map<String, List<Metablock<Link>>> runAllInspections(Layout layout) throws InspectionExecutionError {
        Map<String, List<Metablock<Link>>> inspectionMap = new HashMap<>();
        for (Inspection inspection:layout.getInspections()) {
            LinkBuilder builder = new LinkBuilder(inspection.getName());
            Path tempDir = null;
            try {
                tempDir = Files.createTempDirectory("intoto");
                builder.setBasePath(tempDir.toString());
                builder.addMaterial(Arrays.asList(tempDir.toString()));
                runCommand(inspection.getRun(), tempDir.toFile());
            } catch (IOException | InterruptedException e) {
                throw new InspectionExecutionError(inspection, e.getMessage());
            }
            builder.addProduct(Arrays.asList(tempDir.toString()));
        }
        return inspectionMap;
    }
    
    private static ByProducts runCommand(List<String> command, File workDir) throws InterruptedException, IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // run command
        processBuilder.directory(workDir).command(command);
        Process process = processBuilder.start();
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        BufferedReader stdOutReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = stdOutReader.readLine()) != null) {
            stdout.append(line).append('\n');
        }
        
        String errLine;
        while ((errLine = stdErrorReader.readLine()) != null) {
            stdout.append(errLine).append('\n');
        }

        int exitVal = process.waitFor();
        
        return new ByProducts(stdout.toString(), stderr.toString(), exitVal);
    }   

}
