export interface ScanRequest {
    domain: string
    timeoutMinutes?: number
}

export type ScanStatus = "QUEUED" | "IN_PROGRESS" | "SUCCESS" | "EMPTY_RESULT" | "FAILED" | "TIMEOUT"


export interface ScanEntity {
    id: string
    domain: string
    status: ScanStatus
    result?: string
    startedAt?: string
    finishedAt?: string
    message?: string
}

export interface PaginatedResponse<T> {
    content: T[]
    totalPages: number
    totalElements: number
    number: number
}

export type ScanPaginatedResponse = PaginatedResponse<ScanEntity>
