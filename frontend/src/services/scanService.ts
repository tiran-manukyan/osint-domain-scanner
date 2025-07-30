import axios, { type AxiosError } from "axios"
import type { ScanRequest, ScanEntity, ScanPaginatedResponse } from "../types/scan"

const API_BASE = "http://backend.osint.local:8080/api"

const scanApi = axios.create({
    baseURL: API_BASE,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
})

export const startScan = async (data: ScanRequest): Promise<ScanEntity> => {
    try {
        const response = await scanApi.post<ScanEntity>("/scans", data)
        return response.data
    } catch (error) {
        const axiosError = error as AxiosError
        console.error("Error starting scan:", axiosError.message)
        throw error
    }
}

export const fetchScans = async (page = 0, size = 8): Promise<ScanPaginatedResponse> => {
    try {
        const response = await scanApi.get<ScanPaginatedResponse>("/scans", {
            params: { page, size },
        })
        return response.data
    } catch (error) {
        const axiosError = error as AxiosError
        console.error("Error fetching scans:", axiosError.message)
        throw error
    }
}
