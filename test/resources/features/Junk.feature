# new feature
# Tags: optional
    
Feature: name the feature
    
Scenario: Getting a folder upload works in chrome, not firefox and apparently not others...
    When I visit url /login
    And I populate the view fields:
      | selector            | value           | fieldType  |
      | [name='username']   | test@na.gov.uk  |  input     |
      | [name='password']   | Abcde*12345     |  input     |
    And I click the [value='Sign In'] element
    And I visit url /seriesDetails
    And I populate the view fields:
      | selector            | value           | fieldType    |
      | [id='seriesNo']     | TEST1           |  dropdown    |
    And I click the [type='submit'] element
    And I populate the view fields:
      | selector                    | value             | fieldType  |
      | [name='consignmentName']    | test_consignment  |  input     |
      | [name='transferringBody']   | test_tb           |  input     |
    And I click the [type='submit'] element
    Given the consignment id from the url
    Then I expect the database query getConsignment to contain:
      | field               |  value               |
      |  name               |   test_consignment   |
      |  transferringBody   |   test_tb            |
    And I populate the view fields:
      | selector              | value                                 | fieldType     |
      | [id='file-upload']    | /test/resources/files/test-folder/    |  fileupload   |
    And I sleep 10 seconds
    When I visit url /login
#    And I click the [id='upload-submit'] element


