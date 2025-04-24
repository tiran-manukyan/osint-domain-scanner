import { Dialog, DialogContent, DialogTitle, Typography, Box, Chip, IconButton, Paper, Divider } from "@mui/material"
import CloseIcon from "@mui/icons-material/Close"
import ContentCopyIcon from "@mui/icons-material/ContentCopy"
import type { ScanEntity } from "../types/scan"
import "../styles/ScanModal.css"

interface Props {
    scan: ScanEntity | null
    onClose: () => void
}

export default function ScanModal({ scan, onClose }: Props) {
    const handleCopyResults = () => {
        if (scan?.result) {
            navigator.clipboard.writeText(scan.result)
        }
    }

    const getStatusColor = (status: string) => {
        switch (status.toLowerCase()) {
            case "success":
                return "success"
            case "failed":
                return "error"
            case "in_progress":
                return "warning"
            default:
                return "default"
        }
    }

    return (
        <Dialog
            open={!!scan}
            onClose={onClose}
            fullWidth
            maxWidth="md"
            PaperProps={{
                sx: {
                    borderRadius: 3,
                    overflow: "hidden",
                },
            }}
        >
            <DialogTitle className="scan-modal-title">
                <Box>
                    <Typography variant="h5" className="scan-modal-header-text">
                        Scan Details
                    </Typography>
                    {scan && (
                        <Typography variant="subtitle1" className="scan-modal-domain">
                            {scan.domain}
                        </Typography>
                    )}
                </Box>
                <IconButton onClick={onClose} size="small">
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent className="scan-modal-content">
                {scan && (
                    <>
                        <Box className="scan-info-grid">
                            <Paper className="scan-info-card">
                                <Typography variant="subtitle2" className="scan-info-label">
                                    Status
                                </Typography>
                                <Chip label={scan.status} color={getStatusColor(scan.status)} size="small" />
                            </Paper>

                            <Paper className="scan-info-card">
                                <Typography variant="subtitle2" className="scan-info-label">
                                    Timing
                                </Typography>
                                <Box>
                                    <Box className="timing-info">
                                        <Typography className="timing-label">Started:</Typography>
                                        <Typography>{scan.startedAt}</Typography>
                                    </Box>
                                    <Box className="timing-info">
                                        <Typography className="timing-label">Finished:</Typography>
                                        <Typography>{scan.finishedAt}</Typography>
                                    </Box>
                                </Box>
                            </Paper>
                        </Box>

                        <Divider sx={{ my: 3 }} />

                        <Box className="results-header">
                            <Typography variant="h6" className="results-title">
                                Scan Results
                            </Typography>
                            {scan.result && (
                                <IconButton onClick={handleCopyResults} size="small" title="Copy results">
                                    <ContentCopyIcon fontSize="small" />
                                </IconButton>
                            )}
                        </Box>

                        <Paper className="results-container">
                            {scan.result ? (
                                <Typography component="pre" className="results-text">
                                    {scan.result}
                                </Typography>
                            ) : (
                                <Typography className="no-results">No results available</Typography>
                            )}
                        </Paper>
                    </>
                )}
            </DialogContent>
        </Dialog>
    )
}
