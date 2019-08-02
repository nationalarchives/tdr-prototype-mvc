/// <reference types="Cypress" />

describe('TDR', () => {

  it('should display the sign in button when the root page loads', () => {
    cy.visit('/')
    cy.get('a').should('contain', 'Sign in')
  })

  // it('should fail the test if the test is obviously wrong', () => {
  //   cy.visit('/')
  //   cy.get('a').should('contain', 'Sign ina')
  // })

  it('should display an error if you visit the dashboard page without signing in', () => {
    cy.request({
      url: '/dashboard',
      failOnStatusCode: false
    })
      .then((resp) => {
        expect(resp.status).to.eq(401)
      })
  })

})
