The NullPointerException in your tests is occurring because getPage() is being called on a null object in the searchAigResponse. This usually means that the mock objects are not fully set up, and some parts of the searchAigResponse hierarchy (like Metadata and Page) are not being initialized.

To fix this, you can further mock the nested objects in searchAigResponse. Here’s how you can set up these mocks:

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

    @Mock
    private Metadata metadata;

    @Mock
    private Page page;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setting up the nested mocks
        when(searchAigResponse.getMetadata()).thenReturn(metadata);
        when(metadata.getPage()).thenReturn(page);
    }

    @Test
    void testExportESData_WithSubTerritoryLevel() throws Exception {
        // Set up test data and mocks
        when(searchRequestBody.getQuery()).thenReturn("testQuery");
        when(searchRequestBody.getExportType()).thenReturn("ISSUING_SUB_TERRITORY_LEVEL");
        when(searchRequestBody.getSort()).thenReturn("sortField");
        when(page.getTotalResults()).thenReturn(Constants.ROW_LIMIT - 1);
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
        when(page.getTotalResults()).thenReturn(Constants.ROW_LIMIT - 1);
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
        when(page.getTotalResults()).thenReturn(Constants.ROW_LIMIT + 1);
        when(searchService.search(any())).thenReturn(searchAigResponse);

        // Verify the exception is thrown
        FormSearchFiltersExitsCountException thrown = assertThrows(FormSearchFiltersExitsCountException.class, () -> {
            searchController.exportESData(searchRequest

