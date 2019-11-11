/// <reference types="Cypress" />

describe('TDR - root page & start now button', () => {

  before(() => {
    cy.fixture("user").then(testuser => cy.signUp(testuser));
  })

  beforeEach(() => {
    //any tests using fixture needs to be in a regular functon rather than an arrow function to get binding to "this"
    cy.fixture("user").as("testuser");
  })

  it('should display the start now button when the root page loads', () => {
    cy.visit('/')
    cy.get('a').should('contain', 'Start now')
  })

  it('should take user to the sign in page where they can log in', function() {
    cy.visit('/')
    cy.contains('Start now').click()
    cy.get('h1').should('contain', 'Sign In')
    cy.login(this.testuser)
    cy.get('h1').should('contain', 'Dashboard')
  })

  it('should take user to the dashboard page if already logged in', function()  {
    cy.login(this.testuser)
    cy.visit('/')
    cy.contains('Start now').click()
    cy.get('h1').should('contain', 'Dashboard')
  })

})
