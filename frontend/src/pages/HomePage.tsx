"use client"

import type React from "react"
import {useEffect, useState} from "react"
import {Alert, Box, Container, Pagination, Snackbar, Typography} from "@mui/material"
import {fetchScans} from "../services/scanService"
import type {ScanEntity} from "../types/scan"
import ScanCard from "../components/ScanCard"
import ScanForm from "../components/ScanForm"
import ScanModal from "../components/ScanModal"
import LanguageIcon from "@mui/icons-material/Language"
import "../styles/HomePage.css"

const HomePage: React.FC = () => {
    const [scans, setScans] = useState<ScanEntity[]>([])
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(1)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const [selectedScan, setSelectedScan] = useState<ScanEntity | null>(null)
    const [loading, setLoading] = useState(true)

    const loadScans = async (pageIndex: number) => {
        setLoading(true)
        try {
            const response = await fetchScans(pageIndex)
            if ("content" in response) {
                setScans(response.content)
                setTotalPages(response.totalPages)
                setPage(response.number)
            } else {
                setScans(response)
            }
        } catch {
            setErrorMessage("Failed to load scans")
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadScans(0)
    }, [])

    const handlePageChange = (_: React.ChangeEvent<unknown>, value: number) => {
        loadScans(value - 1)
    }

    return (
        <Box className="page-container">
            <Container maxWidth="lg">
                <Box className="header-container">
                    <Box className="header-icon-container">
                        <LanguageIcon className="header-icon"/>
                    </Box>
                    <Typography variant="h3" component="h1" className="header-title">
                        OSINT Domain Scanner
                    </Typography>
                </Box>

                <ScanForm
                    onScanComplete={async (message) => {
                        try {
                            await loadScans(0)
                            setSuccessMessage(message)
                        } catch {
                            setErrorMessage("Failed to load scans after scan complete.")
                        }
                    }}
                    onScanError={(error) => setErrorMessage(error)}
                />

                <Box mt={5}>
                    <Typography variant="h5" className="section-title">
                        Recent Scans
                        {loading && (
                            <Typography variant="body2" component="span" className="loading-text">
                                Loading...
                            </Typography>
                        )}
                    </Typography>

                    <Box
                        sx={{
                            display: 'grid',
                            gap: 3,
                            gridTemplateColumns: {
                                xs: 'repeat(1, 1fr)',
                                sm: 'repeat(2, 1fr)',
                                md: 'repeat(3, 1fr)',
                                lg: 'repeat(4, 1fr)',
                            },
                            alignItems: 'stretch',
                        }}
                    >
                        {(loading ? [1, 2, 3, 4] : scans).map((item, index) => (
                            <Box
                                key={loading ? `loading-${index}` : `scan-${(item as ScanEntity).id}`}
                                sx={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    justifyContent: 'space-between',
                                    height: '100%',
                                    minHeight: 200,
                                    bgcolor: loading ? 'rgba(0,0,0,0.05)' : 'white',
                                    borderRadius: 3,
                                    border: loading ? 'none' : '1px solid #eee',
                                    boxShadow: 1,
                                    overflow: 'hidden',
                                    cursor: loading ? 'default' : 'pointer',
                                }}
                                onClick={!loading ? () => setSelectedScan(item as ScanEntity) : undefined}
                            >
                                {!loading && <ScanCard scan={item as ScanEntity} onClick={() => setSelectedScan(item as ScanEntity)}/>}
                            </Box>
                        ))}
                    </Box>

                    {totalPages > 1 && (
                        <Box className="pagination-container">
                            <Pagination
                                count={totalPages}
                                page={page + 1}
                                onChange={handlePageChange}
                                color="primary"
                                shape="rounded"
                                size="large"
                                classes={{root: "pagination-item"}}
                            />
                        </Box>
                    )}
                </Box>

                <Snackbar
                    anchorOrigin={{vertical: "top", horizontal: "right"}}
                    open={!!successMessage}
                    autoHideDuration={4000}
                    onClose={() => setSuccessMessage(null)}
                >
                    <Alert severity="success" onClose={() => setSuccessMessage(null)} className="alert">
                        {successMessage}
                    </Alert>
                </Snackbar>

                <Snackbar
                    anchorOrigin={{vertical: "top", horizontal: "right"}}
                    open={!!errorMessage}
                    autoHideDuration={5000}
                    onClose={() => setErrorMessage(null)}
                >
                    <Alert severity="error" onClose={() => setErrorMessage(null)} className="alert">
                        {errorMessage}
                    </Alert>
                </Snackbar>

                <ScanModal scan={selectedScan} onClose={() => setSelectedScan(null)}/>
            </Container>
        </Box>
    )
}

export default HomePage
