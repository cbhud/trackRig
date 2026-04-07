package me.cbhud.TrackRig.service;

/**
 * Service interface for generating Excel (.xlsx) and PDF exports of
 * workstations and components.
 */
public interface ExportService {

    // ========================
    // WORKSTATION EXPORTS
    // ========================

    /**
     * Exports all workstations to an Excel workbook.
     *
     * @param includeComponents if true, a second sheet with all installed
     *                          components is added
     * @param includeLogs       if true, a third sheet with all maintenance logs is
     *                          added
     * @return raw bytes of the .xlsx file
     */
    byte[] exportWorkstationsToExcel(boolean includeComponents, boolean includeLogs);

    /**
     * Exports all workstations to a PDF document.
     *
     * @param includeComponents if true, a components section is included
     * @param includeLogs       if true, a maintenance logs section is included
     * @return raw bytes of the .pdf file
     */
    byte[] exportWorkstationsToPdf(boolean includeComponents, boolean includeLogs);

    // ========================
    // COMPONENT EXPORTS
    // ========================

    /**
     * Exports components to an Excel workbook with optional filters.
     * All filter parameters are optional — passing null means "no filter for this
     * field".
     *
     * @param statusId   filter by component_status.id (null = all statuses)
     * @param categoryId filter by component_category.id (null = all categories)
     * @param inStorage  true = storage only (workstation IS NULL),
     *                   false = assigned only (workstation IS NOT NULL),
     *                   null = all components
     * @return raw bytes of the .xlsx file
     */
    byte[] exportComponentsToExcel(Integer statusId, Integer categoryId, Boolean inStorage);

    /**
     * Exports components to a PDF document with optional filters.
     * Same filter semantics as exportComponentsToExcel.
     *
     * @param statusId   filter by component_status.id
     * @param categoryId filter by component_category.id
     * @param inStorage  storage filter (true/false/null)
     * @return raw bytes of the .pdf file
     */
    byte[] exportComponentsToPdf(Integer statusId, Integer categoryId, Boolean inStorage);
}
