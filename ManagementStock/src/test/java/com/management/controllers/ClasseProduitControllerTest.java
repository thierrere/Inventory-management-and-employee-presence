/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.management.controllers;

import com.management.controllers.util.PaginationHelper;
import com.management.jpa.ClasseProduit;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Thierry
 */
@RunWith(MockitoJUnitRunner.class)
public class ClasseProduitControllerTest {
    
    @InjectMocks
    private ClasseProduitController controllerUT = new ClasseProduitController();
    
    @Mock
    private ClasseProduit currentUT;
    
    public ClasseProduitControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        //when(controllerUT.getSelected()).thenReturn(currentUT);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSelected method, of class ClasseProduitController.
     */
    @Test
    public void testGetSelected() {
        System.out.println("getSelected");
        ClasseProduit expResult = new ClasseProduit();
        ClasseProduit result = controllerUT.getSelected();
        assertEquals(expResult, result);
    }

    /**
     * Test of modifierCurrent method, of class ClasseProduitController.
     */
    @Test
    public void testModifierCurrent() {
        System.out.println("modifierCurrent");
        ClasseProduit c = new ClasseProduit();
        controllerUT.modifierCurrent(c);
        assertEquals(c,currentUT);
    }

    /**
     * Test of afForm method, of class ClasseProduitController.
     */
    @Test
    public void testAfForm() {
        System.out.println("afForm");
        controllerUT.afForm();
        Boolean bol=controllerUT.getBol();
        assertEquals(true,bol);
    }

    /**
     * Test of annuler method, of class ClasseProduitController.
     */
    @Test
    public void testAnnuler() {
        System.out.println("annuler");
        controllerUT.annuler();
        Boolean bol=controllerUT.getBol();
        assertEquals(false,bol);
    }

    /**
     * Test of getBol method, of class ClasseProduitController.
     */
    @Test
    public void testGetBol() {
        System.out.println("getBol");
        Boolean expResult = false;
        Boolean result = controllerUT.getBol();
        assertEquals(expResult, result);
    }

    /**
     * Test of cacheButton method, of class ClasseProduitController.
     */
    @Test
    public void testCacheButton() {
        System.out.println("cacheButton");
        Boolean expResult = true;
        Boolean result = controllerUT.cacheButton();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUpdateForm method, of class ClasseProduitController.
     */
    @Test
    public void testGetUpdateForm() {
        System.out.println("getUpdateForm");
        Boolean expResult = false;
        Boolean result = controllerUT.getUpdateForm();
        assertEquals(expResult, result);
    }

    /**
     * Test of setUpdateForm method, of class ClasseProduitController.
     *
    @Test
    public void testSetUpdateForm() {
        System.out.println("setUpdateForm");
        Boolean updateForm = null;
        ClasseProduitController instance = new ClasseProduitController();
        instance.setUpdateForm(updateForm);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of closeDialogUpdate method, of class ClasseProduitController.
     *
    @Test
    public void testCloseDialogUpdate() {
        System.out.println("closeDialogUpdate");
        ClasseProduitController instance = new ClasseProduitController();
        instance.closeDialogUpdate();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of getAllClasse method, of class ClasseProduitController.
     *
    @Test
    public void testGetAllClasse() {
        System.out.println("getAllClasse");
        ClasseProduitController instance = new ClasseProduitController();
        DataModel<ClasseProduit> expResult = null;
        DataModel<ClasseProduit> result = instance.getAllClasse();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of setAllClasse method, of class ClasseProduitController.
     *
    @Test
    public void testSetAllClasse() {
        System.out.println("setAllClasse");
        DataModel<ClasseProduit> allClasse = null;
        ClasseProduitController instance = new ClasseProduitController();
        instance.setAllClasse(allClasse);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of getPagination method, of class ClasseProduitController.
     *
    @Test
    public void testGetPagination() {
        System.out.println("getPagination");
        ClasseProduitController instance = new ClasseProduitController();
        PaginationHelper expResult = null;
        PaginationHelper result = instance.getPagination();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of prepareList method, of class ClasseProduitController.
     *
    @Test
    public void testPrepareList() {
        System.out.println("prepareList");
        ClasseProduitController instance = new ClasseProduitController();
        String expResult = "";
        String result = instance.prepareList();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of prepareView method, of class ClasseProduitController.
     *
    @Test
    public void testPrepareView() {
        System.out.println("prepareView");
        ClasseProduitController instance = new ClasseProduitController();
        instance.prepareView();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of verifyUnicity method, of class ClasseProduitController.
     *
    @Test
    public void testVerifyUnicity() {
        System.out.println("verifyUnicity");
        String nom = "";
        ClasseProduitController instance = new ClasseProduitController();
        ClasseProduit expResult = null;
        ClasseProduit result = instance.verifyUnicity(nom);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of prepareCreate method, of class ClasseProduitController.
     *
    @Test
    public void testPrepareCreate() {
        System.out.println("prepareCreate");
        ClasseProduitController instance = new ClasseProduitController();
        instance.prepareCreate();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of create method, of class ClasseProduitController.
     *
    @Test
    public void testCreate() {
        System.out.println("create");
        ClasseProduitController instance = new ClasseProduitController();
        instance.create();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getNouveauNom method, of class ClasseProduitController.
     *
    @Test
    public void testGetNouveauNom() {
        System.out.println("getNouveauNom");
        ClasseProduitController instance = new ClasseProduitController();
        String expResult = "";
        String result = instance.getNouveauNom();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of setNouveauNom method, of class ClasseProduitController.
     *
    @Test
    public void testSetNouveauNom() {
        System.out.println("setNouveauNom");
        String nouveauNom = "";
        ClasseProduitController instance = new ClasseProduitController();
        instance.setNouveauNom(nouveauNom);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of prepareEdit method, of class ClasseProduitController.
     *
    @Test
    public void testPrepareEdit() {
        System.out.println("prepareEdit");
        ClasseProduitController instance = new ClasseProduitController();
        instance.prepareEdit();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of getToModify method, of class ClasseProduitController.
     *
    @Test
    public void testGetToModify() {
        System.out.println("getToModify");
        ClasseProduitController instance = new ClasseProduitController();
        ClasseProduit expResult = null;
        ClasseProduit result = instance.getToModify();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of test method, of class ClasseProduitController.
     *
    @Test
    public void testTest() {
        System.out.println("test");
        ClasseProduitController instance = new ClasseProduitController();
        instance.test();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of update method, of class ClasseProduitController.
     *
    @Test
    public void testUpdate() {
        System.out.println("update");
        ClasseProduitController instance = new ClasseProduitController();
        instance.update();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of destroy method, of class ClasseProduitController.
     *
    @Test
    public void testDestroy() {
        System.out.println("destroy");
        ClasseProduitController instance = new ClasseProduitController();
        instance.destroy();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of destroyAndView method, of class ClasseProduitController.
     *
    @Test
    public void testDestroyAndView() {
        System.out.println("destroyAndView");
        ClasseProduitController instance = new ClasseProduitController();
        String expResult = "";
        String result = instance.destroyAndView();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getItems method, of class ClasseProduitController.
     *
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        ClasseProduitController instance = new ClasseProduitController();
        DataModel expResult = null;
        DataModel result = instance.getItems();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of next method, of class ClasseProduitController.
     *
    @Test
    public void testNext() {
        System.out.println("next");
        ClasseProduitController instance = new ClasseProduitController();
        instance.next();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of previous method, of class ClasseProduitController.
     *
    @Test
    public void testPrevious() {
        System.out.println("previous");
        ClasseProduitController instance = new ClasseProduitController();
        instance.previous();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getItemsAvailableSelectMany method, of class ClasseProduitController.
     *
    @Test
    public void testGetItemsAvailableSelectMany() {
        System.out.println("getItemsAvailableSelectMany");
        ClasseProduitController instance = new ClasseProduitController();
        SelectItem[] expResult = null;
        SelectItem[] result = instance.getItemsAvailableSelectMany();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
     */

    /**
     * Test of getItemsAvailableSelectOne method, of class ClasseProduitController.
     *
    @Test
    public void testGetItemsAvailableSelectOne() {
        System.out.println("getItemsAvailableSelectOne");
        ClasseProduitController instance = new ClasseProduitController();
        SelectItem[] expResult = null;
        SelectItem[] result = instance.getItemsAvailableSelectOne();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

    /**
     * Test of getClasseProduit method, of class ClasseProduitController.
     *
    @Test
    public void testGetClasseProduit() {
        System.out.println("getClasseProduit");
        Integer id = null;
        ClasseProduitController instance = new ClasseProduitController();
        ClasseProduit expResult = null;
        ClasseProduit result = instance.getClasseProduit(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
