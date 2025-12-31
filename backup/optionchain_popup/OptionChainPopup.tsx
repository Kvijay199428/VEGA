import React, { useEffect, useState, useCallback, useRef, useMemo } from 'react';
import {
    X,
    Loader2,
    RefreshCw,
    ChevronDown,
    Activity,
    Wifi,
    WifiOff,
    Maximize2,
    Minimize2
} from 'lucide-react';
import api from '../services/api';
import ValuationIcon from './ValuationIcon';
import { BidAskBar } from './charts';
import { handleUpstoxError } from '../utils/upstoxErrors';
import useOptionChainWebSocket from '../hooks/useOptionChainWebSocket';

interface Underlying {
    instrument_key: string;
    name: string;
    trading_symbol?: string;
    exchange: string;
}

interface OptionSide {
    instrument_key: string;
    ltp: number;
    close_price: number;
    volume: number;
    oi: number;
    bid_price: number;
    bid_qty: number;
    ask_price: number;
    ask_qty: number;
    prev_oi: number;
    vega: number;
    theta: number;
    gamma: number;
    delta: number;
    iv: number;
    pop: number;
    valuation?: {
        status: 'Overvalued' | 'Undervalued' | 'Fair';
        fair_price: number;
        market_price: number;
        mispricing_pct: number;
        action: 'SELL' | 'BUY' | 'HOLD';
        blinking: boolean;
        tooltip_details: {
            fair_price_formatted: string;
            market_price_formatted: string;
            mispricing_pct_formatted: string;
            confidence_level: 'High' | 'Medium' | 'Low';
        };
    };
}

interface OptionChainRow {
    expiry: string;
    pcr: number;
    strike_price: number;
    underlying_key: string;
    underlying_spot_price: number;
    call: OptionSide;
    put: OptionSide;
}

interface OptionChainPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

const OptionChainPopup: React.FC<OptionChainPopupProps> = ({ isOpen, onClose }) => {
    const [segment, setSegment] = useState<'index' | 'equity'>('index');
    const [exchange, setExchange] = useState<'NSE' | 'BSE'>('NSE');
    const [underlyings, setUnderlyings] = useState<Underlying[]>([]);
    const [selectedUnderlying, setSelectedUnderlying] = useState<string>('');
    const [expiries, setExpiries] = useState<string[]>([]);
    const [selectedExpiry, setSelectedExpiry] = useState<string>('');
    const [optionChain, setOptionChain] = useState<OptionChainRow[]>([]);
    const [spotPrice, setSpotPrice] = useState<number>(0);
    const [isLoading, setIsLoading] = useState(false);
    const [isFullscreen, setIsFullscreen] = useState(false);

    const {
        isConnected: wsConnected,
        isLive,
        data: wsData,
        spotPrice: wsSpotPrice,
        updatedAt: wsUpdatedAt,
        setDataFromHttp
    } = useOptionChainWebSocket(selectedUnderlying || null, selectedExpiry || null);

    const tableContainerRef = useRef<HTMLDivElement>(null);

    const atmIndex = useMemo(() => {
        if (!spotPrice || optionChain.length === 0) return -1;
        let closestIndex = 0;
        let closestDiff = Math.abs(optionChain[0].strike_price - spotPrice);
        optionChain.forEach((row, index) => {
            const diff = Math.abs(row.strike_price - spotPrice);
            if (diff < closestDiff) {
                closestDiff = diff;
                closestIndex = index;
            }
        });
        return closestIndex;
    }, [optionChain, spotPrice]);

    const atmStrike = useMemo(() => {
        return atmIndex >= 0 ? optionChain[atmIndex]?.strike_price : 0;
    }, [atmIndex, optionChain]);

    const getStrikeInfo = (strike: number) => {
        if (!spotPrice) return { isATM: false, isITM: false, isOTM: false };
        const isATM = strike === atmStrike;
        const isITM = strike < spotPrice;
        const isOTM = strike > spotPrice;
        return { isATM, isITM, isOTM };
    };

    const fetchUnderlyings = useCallback(async () => {
        try {
            const response = await api.get(`/v1/options/underlyings?segment=${segment}&exchange=${exchange}`);
            if (response.data?.status === 'success') {
                setUnderlyings(response.data.data || []);
                if (response.data.data?.length > 0) {
                    setSelectedUnderlying(response.data.data[0].instrument_key);
                }
            }
        } catch (err) {
            console.error('Error fetching underlyings:', err);
            handleUpstoxError(err, 'Options Underlyings');
        }
    }, [segment, exchange]);

    const fetchExpiries = useCallback(async () => {
        if (!selectedUnderlying) return;
        try {
            const response = await api.get(`/v1/options/expiries/${encodeURIComponent(selectedUnderlying)}`);
            if (response.data?.status === 'success') {
                setExpiries(response.data.data || []);
                if (response.data.data?.length > 0) {
                    setSelectedExpiry(response.data.data[0]);
                }
            }
        } catch (err) {
            console.error('Error fetching expiries:', err);
            handleUpstoxError(err, 'Options Expiries');
        }
    }, [selectedUnderlying]);

    const fetchOptionChain = useCallback(async (forceRefresh = false) => {
        if (!selectedUnderlying || !selectedExpiry) return;
        setIsLoading(true);
        try {
            const response = await api.get('/v1/options/chain', {
                params: {
                    instrument_key: selectedUnderlying,
                    expiry_date: selectedExpiry,
                    force_refresh: forceRefresh
                }
            });
            if (response.data?.status === 'success') {
                const data = response.data.data || [];
                setOptionChain(data);
                setDataFromHttp(data, response.data.updated_at);
                if (data.length > 0) {
                    setSpotPrice(data[0].underlying_spot_price);
                }
            }
        } catch (err: any) {
            console.error('Error fetching option chain:', err);
            handleUpstoxError(err, 'Option Chain');
        } finally {
            setIsLoading(false);
        }
    }, [selectedUnderlying, selectedExpiry, setDataFromHttp]);

    useEffect(() => {
        if (isOpen) {
            fetchUnderlyings();
        }
    }, [isOpen, fetchUnderlyings]);

    useEffect(() => {
        if (selectedUnderlying) {
            fetchExpiries();
        }
    }, [selectedUnderlying, fetchExpiries]);

    useEffect(() => {
        if (selectedUnderlying && selectedExpiry) {
            fetchOptionChain();
        }
    }, [selectedUnderlying, selectedExpiry, fetchOptionChain]);

    useEffect(() => {
        if (wsData && wsData.length > 0 && isLive) {
            setOptionChain(wsData);
            if (wsSpotPrice) setSpotPrice(wsSpotPrice);
        }
    }, [wsData, wsSpotPrice, wsUpdatedAt, isLive]);

    useEffect(() => {
        if (optionChain.length > 0 && spotPrice && atmIndex >= 0) {
            const timer = setTimeout(() => {
                const atmRow = document.getElementById(`popup-strike-row-${atmIndex}`);
                if (atmRow) {
                    atmRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }, 100);
            return () => clearTimeout(timer);
        }
    }, [optionChain, spotPrice, atmIndex]);

    const formatNumber = (num: number, decimals: number = 2) => num?.toFixed(decimals) || '-';
    const formatLargeNumber = (num: number) => {
        if (num >= 10000000) return (num / 10000000).toFixed(2) + ' Cr';
        if (num >= 100000) return (num / 100000).toFixed(2) + ' L';
        if (num >= 1000) return (num / 1000).toFixed(2) + ' K';
        return num?.toString() || '-';
    };

    const getCallSideClass = (strike: number) => {
        if (!spotPrice) return '';
        const { isATM, isITM, isOTM } = getStrikeInfo(strike);
        if (isATM) return 'bg-amber-200 dark:bg-amber-800/50';
        if (isITM) return 'bg-green-50 dark:bg-green-900/20';
        if (isOTM) return 'bg-red-50 dark:bg-red-900/20';
        return '';
    };

    const getPutSideClass = (strike: number) => {
        if (!spotPrice) return '';
        const { isATM, isITM, isOTM } = getStrikeInfo(strike);
        if (isATM) return 'bg-amber-200 dark:bg-amber-800/50';
        if (isITM) return 'bg-red-50 dark:bg-red-900/20';
        if (isOTM) return 'bg-green-50 dark:bg-green-900/20';
        return '';
    };

    const getChangeColor = (val: number) => {
        if (val > 0) return 'text-green-600 dark:text-green-400';
        if (val < 0) return 'text-red-600 dark:text-red-400';
        return '';
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
                onClick={onClose}
            />

            {/* Modal */}
            <div
                className={`relative bg-slate-900 rounded-xl shadow-2xl border border-slate-700 flex flex-col transition-all duration-300 ${isFullscreen
                        ? 'w-full h-full rounded-none'
                        : 'w-[95vw] h-[90vh] max-w-[1800px]'
                    }`}
            >
                {/* Header */}
                <div className="flex items-center justify-between px-4 py-3 border-b border-slate-700 flex-shrink-0">
                    <div className="flex items-center gap-4">
                        {/* Segment Tabs */}
                        <div className="flex gap-2">
                            {(['index', 'equity'] as const).map((seg) => (
                                <button
                                    key={seg}
                                    onClick={() => setSegment(seg)}
                                    className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${segment === seg
                                        ? 'bg-primary-600 text-white'
                                        : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                                        }`}
                                >
                                    {seg === 'index' ? 'Index' : 'Equity'}
                                </button>
                            ))}
                        </div>

                        {/* Exchange Toggle */}
                        <div className="flex gap-1">
                            {(['NSE', 'BSE'] as const).map((ex) => (
                                <button
                                    key={ex}
                                    onClick={() => setExchange(ex)}
                                    className={`px-2 py-1 rounded text-xs font-medium transition-colors ${exchange === ex
                                        ? 'bg-primary-600 text-white'
                                        : 'bg-slate-800 text-slate-400'
                                        }`}
                                >
                                    {ex}
                                </button>
                            ))}
                        </div>

                        {/* Underlying Selector */}
                        <div className="relative">
                            <select
                                value={selectedUnderlying}
                                onChange={(e) => setSelectedUnderlying(e.target.value)}
                                className="appearance-none bg-slate-800 px-3 py-1.5 pr-7 rounded-lg text-sm font-medium border-none outline-none cursor-pointer text-white"
                            >
                                {underlyings.map((u) => (
                                    <option key={u.instrument_key} value={u.instrument_key}>
                                        {u.name}
                                    </option>
                                ))}
                            </select>
                            <ChevronDown size={14} className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400" />
                        </div>

                        {/* Expiry Selector */}
                        <div className="relative">
                            <select
                                value={selectedExpiry}
                                onChange={(e) => setSelectedExpiry(e.target.value)}
                                className="appearance-none bg-slate-800 px-3 py-1.5 pr-7 rounded-lg text-sm font-medium border-none outline-none cursor-pointer text-white"
                            >
                                {expiries.map((exp) => (
                                    <option key={exp} value={exp}>
                                        {new Date(exp).toLocaleDateString('en-IN', {
                                            day: '2-digit',
                                            month: 'short',
                                            year: 'numeric'
                                        })}
                                    </option>
                                ))}
                            </select>
                            <ChevronDown size={14} className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400" />
                        </div>

                        {/* Spot Price */}
                        {spotPrice > 0 && (
                            <div className="flex items-center gap-2 px-3 py-1.5 bg-primary-900/30 rounded-lg">
                                <Activity size={14} className="text-primary-400" />
                                <span className="text-sm font-medium text-primary-400">
                                    ₹{spotPrice.toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                                </span>
                            </div>
                        )}

                        {/* Live Status */}
                        <div className={`flex items-center gap-1.5 px-2 py-1 rounded ${isLive
                            ? 'bg-green-900/30 text-green-400'
                            : wsConnected
                                ? 'bg-yellow-900/30 text-yellow-400'
                                : 'bg-slate-800 text-slate-500'
                            }`}>
                            {wsConnected ? <Wifi size={12} className={isLive ? 'animate-pulse' : ''} /> : <WifiOff size={12} />}
                            <span className="text-xs font-medium">{isLive ? 'LIVE' : wsConnected ? 'Connected' : 'Offline'}</span>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => fetchOptionChain(true)}
                            disabled={isLoading}
                            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
                            title="Force refresh"
                        >
                            <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''} />
                        </button>
                        <button
                            onClick={() => setIsFullscreen(!isFullscreen)}
                            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
                            title={isFullscreen ? 'Exit fullscreen' : 'Fullscreen'}
                        >
                            {isFullscreen ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
                        </button>
                        <button
                            onClick={onClose}
                            className="p-2 rounded-lg bg-slate-800 hover:bg-red-600 transition-colors"
                            title="Close"
                        >
                            <X size={16} />
                        </button>
                    </div>
                </div>

                {/* Table Content */}
                <div ref={tableContainerRef} className="flex-1 overflow-auto">
                    {isLoading ? (
                        <div className="flex items-center justify-center py-12">
                            <Loader2 className="animate-spin h-8 w-8 text-primary-600" />
                        </div>
                    ) : (
                        <table className="w-full option-chain-table text-xs">
                            <thead className="sticky top-0 z-10">
                                <tr className="bg-slate-800">
                                    <th colSpan={13} className="px-2 py-1.5 text-center text-green-400 font-semibold border-b border-slate-700">CALLS</th>
                                    <th className="px-2 py-1.5 bg-slate-900 border-b border-slate-700"></th>
                                    <th colSpan={13} className="px-2 py-1.5 text-center text-red-400 font-semibold border-b border-slate-700">PUTS</th>
                                </tr>
                                <tr className="bg-slate-800 text-slate-400">
                                    <th className="px-1 py-1.5 text-right">Delta</th>
                                    <th className="px-1 py-1.5 text-right">Gamma</th>
                                    <th className="px-1 py-1.5 text-right">Theta</th>
                                    <th className="px-1 py-1.5 text-right">Vega</th>
                                    <th className="px-1 py-1.5 text-right text-green-400">Bid</th>
                                    <th className="px-1 py-1.5 text-center">B/A</th>
                                    <th className="px-1 py-1.5 text-right text-red-400">Ask</th>
                                    <th className="px-1 py-1.5 text-right">IV</th>
                                    <th className="px-1 py-1.5 text-center">Val</th>
                                    <th className="px-1 py-1.5 text-right">OI</th>
                                    <th className="px-1 py-1.5 text-right">Chg%</th>
                                    <th className="px-1 py-1.5 text-right font-semibold">LTP</th>
                                    <th className="px-1 py-1.5 text-right">Vol</th>
                                    <th className="px-2 py-1.5 text-center font-bold bg-slate-900">STRIKE</th>
                                    <th className="px-1 py-1.5 text-left">Vol</th>
                                    <th className="px-1 py-1.5 text-left font-semibold">LTP</th>
                                    <th className="px-1 py-1.5 text-left">Chg%</th>
                                    <th className="px-1 py-1.5 text-left">OI</th>
                                    <th className="px-1 py-1.5 text-center">Val</th>
                                    <th className="px-1 py-1.5 text-left">IV</th>
                                    <th className="px-1 py-1.5 text-left text-red-400">Ask</th>
                                    <th className="px-1 py-1.5 text-center">B/A</th>
                                    <th className="px-1 py-1.5 text-left text-green-400">Bid</th>
                                    <th className="px-1 py-1.5 text-left">Vega</th>
                                    <th className="px-1 py-1.5 text-left">Theta</th>
                                    <th className="px-1 py-1.5 text-left">Gamma</th>
                                    <th className="px-1 py-1.5 text-left">Delta</th>
                                </tr>
                            </thead>
                            <tbody>
                                {optionChain.length === 0 ? (
                                    <tr>
                                        <td colSpan={27} className="text-center py-8 text-slate-500">
                                            {selectedUnderlying && selectedExpiry
                                                ? 'No option chain data available'
                                                : 'Select an underlying and expiry'}
                                        </td>
                                    </tr>
                                ) : (
                                    optionChain.map((row, i) => {
                                        const callLtp = row.call?.ltp || 0;
                                        const callClose = row.call?.close_price || 0;
                                        const callChange = callClose ? ((callLtp - callClose) / callClose * 100) : 0;
                                        const putLtp = row.put?.ltp || 0;
                                        const putClose = row.put?.close_price || 0;
                                        const putChange = putClose ? ((putLtp - putClose) / putClose * 100) : 0;
                                        const callClass = getCallSideClass(row.strike_price);
                                        const putClass = getPutSideClass(row.strike_price);

                                        return (
                                            <tr
                                                key={i}
                                                id={`popup-strike-row-${i}`}
                                                className="border-b border-slate-800 hover:bg-slate-800/50"
                                            >
                                                {/* Call Side */}
                                                <td className={`px-1 py-1 text-right text-slate-400 ${callClass}`}>{formatNumber(row.call?.delta || 0, 3)}</td>
                                                <td className={`px-1 py-1 text-right text-slate-400 ${callClass}`}>{formatNumber(row.call?.gamma || 0, 4)}</td>
                                                <td className={`px-1 py-1 text-right text-slate-400 ${callClass}`}>{formatNumber(row.call?.theta || 0, 1)}</td>
                                                <td className={`px-1 py-1 text-right text-slate-400 ${callClass}`}>{formatNumber(row.call?.vega || 0, 3)}</td>
                                                <td className={`px-1 py-1 text-right text-green-500 ${callClass}`}>{formatNumber(row.call?.bid_price || 0)}</td>
                                                <td className={`px-0.5 py-1 ${callClass}`}>
                                                    <BidAskBar bidQty={row.call?.bid_qty || 0} askQty={row.call?.ask_qty || 0} variant="call" width={40} height={12} />
                                                </td>
                                                <td className={`px-1 py-1 text-right text-red-500 ${callClass}`}>{formatNumber(row.call?.ask_price || 0)}</td>
                                                <td className={`px-1 py-1 text-right text-slate-400 ${callClass}`}>{formatNumber(row.call?.iv || 0, 1)}%</td>
                                                <td className={`px-1 py-1 text-center ${callClass}`}>
                                                    {row.call?.valuation && (
                                                        <ValuationIcon
                                                            status={row.call.valuation.status}
                                                            fairPrice={row.call.valuation.fair_price}
                                                            marketPrice={row.call.valuation.market_price}
                                                            mispricing_pct={row.call.valuation.mispricing_pct}
                                                            action={row.call.valuation.action}
                                                            blinking={row.call.valuation.blinking}
                                                            strikePrice={row.strike_price}
                                                            confidence={row.call.valuation.tooltip_details.confidence_level}
                                                            tooltipDetails={row.call.valuation.tooltip_details}
                                                        />
                                                    )}
                                                </td>
                                                <td className={`px-1 py-1 text-right text-green-400 ${callClass}`}>{formatLargeNumber(row.call?.oi || 0)}</td>
                                                <td className={`px-1 py-1 text-right font-medium ${getChangeColor(callChange)} ${callClass}`}>{callChange >= 0 ? '+' : ''}{formatNumber(callChange, 1)}%</td>
                                                <td className={`px-1 py-1 text-right font-semibold text-green-400 ${callClass}`}>{formatNumber(callLtp)}</td>
                                                <td className={`px-1 py-1 text-right text-slate-500 ${callClass}`}>{formatLargeNumber(row.call?.volume || 0)}</td>

                                                {/* Strike */}
                                                <td className={`px-2 py-1 text-center font-bold ${getStrikeInfo(row.strike_price).isATM ? 'bg-amber-800/50 text-amber-300' : 'bg-slate-900 text-white'}`}>
                                                    {row.strike_price}
                                                    {getStrikeInfo(row.strike_price).isATM && <span className="ml-1 text-[9px] text-amber-400">ATM</span>}
                                                </td>

                                                {/* Put Side */}
                                                <td className={`px-1 py-1 text-left text-slate-500 ${putClass}`}>{formatLargeNumber(row.put?.volume || 0)}</td>
                                                <td className={`px-1 py-1 text-left font-semibold text-red-400 ${putClass}`}>{formatNumber(putLtp)}</td>
                                                <td className={`px-1 py-1 text-left font-medium ${getChangeColor(putChange)} ${putClass}`}>{putChange >= 0 ? '+' : ''}{formatNumber(putChange, 1)}%</td>
                                                <td className={`px-1 py-1 text-left text-red-400 ${putClass}`}>{formatLargeNumber(row.put?.oi || 0)}</td>
                                                <td className={`px-1 py-1 text-center ${putClass}`}>
                                                    {row.put?.valuation && (
                                                        <ValuationIcon
                                                            status={row.put.valuation.status}
                                                            fairPrice={row.put.valuation.fair_price}
                                                            marketPrice={row.put.valuation.market_price}
                                                            mispricing_pct={row.put.valuation.mispricing_pct}
                                                            action={row.put.valuation.action}
                                                            blinking={row.put.valuation.blinking}
                                                            strikePrice={row.strike_price}
                                                            confidence={row.put.valuation.tooltip_details.confidence_level}
                                                            tooltipDetails={row.put.valuation.tooltip_details}
                                                        />
                                                    )}
                                                </td>
                                                <td className={`px-1 py-1 text-left text-slate-400 ${putClass}`}>{formatNumber(row.put?.iv || 0, 1)}%</td>
                                                <td className={`px-1 py-1 text-left text-red-500 ${putClass}`}>{formatNumber(row.put?.ask_price || 0)}</td>
                                                <td className={`px-0.5 py-1 ${putClass}`}>
                                                    <BidAskBar bidQty={row.put?.bid_qty || 0} askQty={row.put?.ask_qty || 0} variant="put" width={40} height={12} />
                                                </td>
                                                <td className={`px-1 py-1 text-left text-green-500 ${putClass}`}>{formatNumber(row.put?.bid_price || 0)}</td>
                                                <td className={`px-1 py-1 text-left text-slate-400 ${putClass}`}>{formatNumber(row.put?.vega || 0, 3)}</td>
                                                <td className={`px-1 py-1 text-left text-slate-400 ${putClass}`}>{formatNumber(row.put?.theta || 0, 1)}</td>
                                                <td className={`px-1 py-1 text-left text-slate-400 ${putClass}`}>{formatNumber(row.put?.gamma || 0, 4)}</td>
                                                <td className={`px-1 py-1 text-left text-slate-400 ${putClass}`}>{formatNumber(row.put?.delta || 0, 3)}</td>
                                            </tr>
                                        );
                                    })
                                )}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default OptionChainPopup;
