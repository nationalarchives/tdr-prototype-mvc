/// <reference types="Cypress" />


describe('TDR - upload page', () => {

  before(() => {
    cy.fixture("user").then(testuser => cy.signUp(testuser));
  })

  beforeEach(() => {
    //any tests using fixture needs to be in a regular functon rather than an arrow function to get binding to "this"
    cy.fixture("user").as("testuser")
  })

  it('should have correct title', function () {
    cy.login(this.testuser)
    cy.visit('/seriesDetails')
    cy.get('#seriesNo').select("TEST1")
    cy.contains("Continue").click()
    cy.get('#consignmentName').type("CON1")
    cy.get('#transferringBody').type("TB")
    cy.contains("Continue").click()
//    cy.get('#file-upload').click()
    cy.readFile("/home/mkingsbury/git/tdr-prototype-mvc/cypress-tests/cypress/fixtures/testfile.txt").should('eq', 'test file content')  
//    cy.get('#file-upload-form').submit()

    cy.fixture("testfile.txt").then(fileContent )




  })

})
