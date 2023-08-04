Feature: Chrome Profiling Test
  Scenario: Google search page profiling test
    Given Open website 'https://www.google.com/'
    And Click "Accept All" button if exists
    And Enter search text "sigiriya"
    And Click search button
    Then Get Images results
    And The page title should contain "sigiriya"
    And Click picture 1
    And Wait 1 seconds
    And Click picture 2
    And Wait 6 seconds


