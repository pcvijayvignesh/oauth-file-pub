To write a test case for this exportESData method in SearchControllerTest.java, you would generally mock the dependencies and verify the interactions and output. Below is a basic example in JUnit, using Mockito to mock dependencies and verify behavior.

Here’s a sample test case:

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class SearchControllerTest {

    @InjectMocks
    private SearchController searchController;

    @Mock
    private SearchService searchService;

    @Mock
    private ExcelExportService excelExportService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SearchRequestBody searchRequestBody;

    @Mock
    private SearchAigResponse searchAigResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExportESData_WithSubTerritoryLevel() throws Exception {
        // Set up test data and mocks
        when(searchRequestBody.getQuery()).thenReturn("testQuery");
        when(searchRequestBody.getExportType()).thenReturn("ISSUING_SUB_TERRITORY_LEVEL");
        when(searchRequestBody.getSort()).thenReturn("sortField");
        when(searchAigResponse.getMetadata().getPage().getTotalResults()).thenReturn(Constants.ROW_LIMIT - 1);
        when(searchService.search(any())).thenReturn(searchAigResponse);

        // Invoke the method under test
        ResponseEntity<SearchAigResponse> response = searchController.exportESData(searchRequestBody);

        // Verify the correct method was called
        verify(excelExportService).issuingSubTerritoryLevelReport(eq(searchAigResponse), eq(httpServletResponse));
        assertNotNull(response);
        assertEquals(searchAigResponse, response.getBody());
    }

    @Test
    void testExportESData_WithFormLevel() throws Exception {
        // Set up test data and mocks
        when(searchRequestBody.getQuery()).thenReturn("testQuery");
        when(searchRequestBody.getExportType()).thenReturn("FORM_LEVEL");
        when(searchRequestBody.getSort()).thenReturn("sortField");
        when(searchAigResponse.getMetadata().getPage().getTotalResults()).thenReturn(Constants.ROW_LIMIT - 1);
        when(searchService.search(any())).thenReturn(searchAigResponse);

        // Invoke the method under test
        ResponseEntity<SearchAigResponse> response = searchController.exportESData(searchRequestBody);

        // Verify the correct method was called
        verify(excelExportService).formLevelReport(eq(searchAigResponse), eq(httpServletResponse));
        assertNotNull(response);
        assertEquals(searchAigResponse, response.getBody());
    }

    @Test
    void testExportESData_TooManyRecords() {
        // Set up test data and mocks
        when(searchRequestBody.getQuery()).thenReturn("testQuery");
        when(searchAigResponse.getMetadata().getPage().getTotalResults()).thenReturn(Constants.ROW_LIMIT + 1);
        when(searchService.search(any())).thenReturn(searchAigResponse);

        // Verify the exception is thrown
        FormSearchFiltersExitsCountException thrown = assertThrows(FormSearchFiltersExitsCountException.class, () -> {
            searchController.exportESData(searchRequestBody);
        });

        assertEquals(Constants.TOO_MUCH_RECORDS_ERR_MSG, thrown.getMessage());
    }
}

Explanation:

1. Test Initialization:

We initialize the mocks and inject them into SearchController.



2. testExportESData_WithSubTerritoryLevel:

Sets up searchRequestBody to return "ISSUING_SUB_TERRITORY_LEVEL" for getExportType.

Mocks searchService.search to return a mocked searchAigResponse.

Calls exportESData and verifies if issuingSubTerritoryLevelReport in excelExportService was called.



3. testExportESData_WithFormLevel:

Similar to the above but sets getExportType to a different value ("FORM_LEVEL") and verifies if formLevelReport was called.



4. testExportESData_TooManyRecords:

Sets up a response where the result count exceeds ROW_LIMIT, expecting FormSearchFiltersExitsCountException.




This structure covers typical scenarios: different export types and an exception case for too many records. Adjust the details to fit your actual classes and constants.

