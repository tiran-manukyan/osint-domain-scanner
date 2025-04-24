import type React from "react"
import {Box, Card, CardActionArea, CardContent, Chip, Typography} from "@mui/material"
import type {ScanEntity} from "../types/scan"
import LanguageIcon from "@mui/icons-material/Language"
import AccessTimeIcon from "@mui/icons-material/AccessTime"
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline"
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline"
import "../styles/ScanCard.css"

interface Props {
    scan: ScanEntity
    onClick: () => void
}

const ScanCard: React.FC<Props> = ({scan, onClick}) => {
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

    const getStatusIcon = (status: string) => {
        switch (status.toLowerCase()) {
            case "success":
                return <CheckCircleOutlineIcon fontSize="small"/>
            case "failed":
                return <ErrorOutlineIcon fontSize="small"/>
            case "in_progress":
                return <AccessTimeIcon fontSize="small"/>
            default:
                return undefined
        }
    }

    return (
        <Card className="scan-card">
            <CardActionArea onClick={onClick}>
                <CardContent className="scan-card-content">
                    <Box className="domain-header">
                        <Box className="domain-icon-container">
                            <LanguageIcon color="primary"/>
                        </Box>
                        <Typography className="domain-title">{scan.domain}</Typography>
                    </Box>

                    <Chip
                        icon={getStatusIcon(scan.status)}
                        label={scan.status}
                        size="small"
                        color={getStatusColor(scan.status)}
                        className="status-chip"
                    />

                    <Box className="scan-details">
                        <Typography className="detail-label">Started:</Typography>
                        <Typography>{scan.startedAt}</Typography>

                        <Typography className="detail-label">Finished:</Typography>
                        <Typography>{scan.finishedAt}</Typography>
                    </Box>

                    <Typography className="view-details-hint">Click to view details</Typography>
                </CardContent>
            </CardActionArea>
        </Card>
    )
}

export default ScanCard
