/* tslint:disable */

// ====================================================
// GraphQL query operation: Consignments
// ====================================================

export interface Consignments_consignments {
    __typename: "Consignment";
    /**
     * The ID of an object
     */
    id: string;
    /**
     * The name of this Consignment
     */
    name: string | null;
  }
  
  export interface Consignments {
    consignments: (Consignments_consignments | null)[] | null;
  }
  