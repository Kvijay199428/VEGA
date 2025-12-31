import { AxiosError } from 'axios'

/**
 * Standardized error response from backend.
 */
export interface ApiError {
    code: string
    message: string
    status: number
    timestamp?: string
}

/**
 * Maps Axios errors to standardized ApiError format.
 */
export function mapError(error: AxiosError): ApiError {
    const status = error.response?.status || 500
    const data = error.response?.data as Record<string, unknown> | undefined

    // Handle known error formats
    if (data && typeof data === 'object') {
        return {
            code: (data.code as string) || 'UNKNOWN_ERROR',
            message: (data.message as string) || error.message,
            status,
            timestamp: new Date().toISOString()
        }
    }

    // Default error mapping
    const errorMap: Record<number, ApiError> = {
        400: { code: 'BAD_REQUEST', message: 'Invalid request', status: 400 },
        401: { code: 'UNAUTHORIZED', message: 'Session expired', status: 401 },
        403: { code: 'FORBIDDEN', message: 'Access denied', status: 403 },
        404: { code: 'NOT_FOUND', message: 'Resource not found', status: 404 },
        429: { code: 'RATE_LIMITED', message: 'Too many requests', status: 429 },
        500: { code: 'SERVER_ERROR', message: 'Internal server error', status: 500 },
        502: { code: 'BAD_GATEWAY', message: 'Broker unavailable', status: 502 },
        503: { code: 'SERVICE_UNAVAILABLE', message: 'Service temporarily unavailable', status: 503 }
    }

    return errorMap[status] || {
        code: 'UNKNOWN_ERROR',
        message: error.message || 'An unexpected error occurred',
        status
    }
}
