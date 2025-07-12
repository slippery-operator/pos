# TSV Upload Functionality Changes

## Overview
This document describes the changes made to the TSV upload functionality for both Product and Inventory endpoints. The main changes include:

1. **All-or-Nothing Validation**: No rows are saved if any validation errors exist
2. **TSV Response Format**: Returns TSV format instead of JSON with validation results
3. **Simplified Architecture**: Removed complex partial-save logic

## Changes Made

### 1. New Utility Class: `TsvResponseUtil`
- **Location**: `pos/src/main/java/com/increff/pos/util/TsvResponseUtil.java`
- **Purpose**: Generates TSV response format with validation results
- **Methods**:
  - `generateInventoryTsvResponse()`: Creates TSV response for inventory uploads
  - `generateProductTsvResponse()`: Creates TSV response for product uploads

### 2. Modified Controllers

#### InventoryController
- **Changed**: `/inventory/upload` endpoint now returns `ResponseEntity<String>` instead of `TsvUploadResponse<InventoryResponse>`
- **Response Format**: TSV file with format: `productId, quantity, remarks`

#### ProductController  
- **Changed**: `/products/upload` endpoint now returns `ResponseEntity<String>` instead of `TsvUploadResponse<ProductResponse>`
- **Response Format**: TSV file with format: `barcode, clientId, name, mrp, imageUrl, remarks`

### 3. Modified DTOs

#### InventoryDto
- **Method**: `uploadInventoryTsv()` 
- **Changes**:
  - Validates all forms first
  - Only saves data if NO validation errors exist
  - Returns TSV format with validation results
  - Uses new `validateInventoryWithoutSaving()` method

#### ProductDto
- **Method**: `uploadProductsTsv()`
- **Changes**:
  - Validates all forms first
  - Only saves data if NO validation errors exist  
  - Returns TSV format with validation results
  - Uses new `validateProductTsvUpload()` method

### 4. Modified API Layer

#### InventoryApi
- **New Method**: `validateInventoryWithoutSaving()`
- **Purpose**: Validates inventory items without saving to database
- **Used for**: All-or-nothing validation approach

### 5. Modified Flow Layer

#### ProductFlow
- **New Method**: `validateProductTsvUpload()`
- **Purpose**: Validates product TSV upload without saving to database
- **Used for**: All-or-nothing validation approach

## Response Format Examples

### Inventory Upload Response
```tsv
productId	quantity	remarks
1	100	valid
2	50	valid
999	25	Product not found with id: 999
3	-10	Quantity must be positive
```

### Product Upload Response
```tsv
barcode	clientId	name	mrp	imageUrl	remarks
TEST001	1	Test Product 1	100.0		valid
TEST002	999	Test Product 2	200.0	http://example.com/image.jpg	Client not found
TEST003	1	Test Product 3	-50.0		MRP must be positive
DUPLICATE	1	Test Product 4	150.0		Duplicate barcode 'DUPLICATE' found in upload file
DUPLICATE	1	Test Product 5	250.0		Duplicate barcode 'DUPLICATE' found in upload file
```

## Key Benefits

1. **Consistency**: All-or-nothing approach ensures data integrity
2. **User-Friendly**: TSV response format is easily readable and can be imported back
3. **Simplified Logic**: Removed complex partial-save handling
4. **Better Error Reporting**: Clear indication of which rows have issues and why
5. **Follows Architecture**: Maintains proper layering conventions

## Usage

### Testing with cURL

#### Inventory Upload
```bash
curl -X POST \
  -H "Content-Type: multipart/form-data" \
  -F "file=@inventory.tsv" \
  http://localhost:8080/inventory/upload \
  -o inventory_results.tsv
```

#### Product Upload
```bash
curl -X POST \
  -H "Content-Type: multipart/form-data" \
  -F "file=@products.tsv" \
  http://localhost:8080/products/upload \
  -o product_results.tsv
```

## Test Files

Test TSV files are provided in `pos/src/test/resources/`:
- `test_inventory.tsv`: Sample inventory data with validation errors
- `test_products.tsv`: Sample product data with validation errors

## Architecture Compliance

The changes follow the established layering conventions:
- **Controller Layer**: Handles HTTP requests/responses
- **DTO Layer**: Performs validation and orchestration
- **Flow Layer**: Coordinates multiple APIs (products only)
- **API Layer**: Contains business logic and validation
- **DAO Layer**: Database operations

No layer exceeds 200 lines of code, and business logic is properly separated across layers. 