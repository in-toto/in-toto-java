package io.github.in_toto.models.layout;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.in_toto.models.Metablock;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class TestLayoutModel {

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
    public void equalsContractLayout() {
        EqualsVerifier.forClass(Layout.class).verify();
    }
    
    @Test
    public void equalsContractSupplyChainItem() {
        EqualsVerifier.forClass(SupplyChainItem.class)
            .usingGetClass().verify();
    }
    
    @Test
    public void equalsContractStep() {
        EqualsVerifier.forClass(Step.class)
            .withRedefinedSuperclass().verify();
    }
    
    @Test
    public void equalsContractInspection() {
        EqualsVerifier.forClass(Inspection.class)
            .withRedefinedSuperclass().verify();
    }

}
