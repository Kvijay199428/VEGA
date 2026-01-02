/**
 * Freeze Control Store - Panel Freeze State Management
 * 
 * Centralized freeze state for all panels and instruments.
 * Supports panel-level and instrument-level freezing.
 */

import { create } from 'zustand';

type PanelType = 'orderBook' | 'greeks' | 'ohlc' | 'metrics' | 'ltpc';

interface FreezeState {
    // Panel-level freeze (affects all instruments)
    panelFreeze: Set<PanelType>;

    // Instrument-level freeze per panel
    instrumentFreeze: Map<PanelType, Set<string>>;

    // Actions
    freezePanel: (panel: PanelType) => void;
    resumePanel: (panel: PanelType) => void;
    isPanelFrozen: (panel: PanelType) => boolean;

    freezeInstrument: (panel: PanelType, instrumentKey: string) => void;
    resumeInstrument: (panel: PanelType, instrumentKey: string) => void;
    isInstrumentFrozen: (panel: PanelType, instrumentKey: string) => boolean;

    // Check if updates should be skipped
    shouldSkipUpdate: (panel: PanelType, instrumentKey: string) => boolean;

    // Bulk operations
    freezeAll: () => void;
    resumeAll: () => void;
}

export const useFreezeControlStore = create<FreezeState>((set, get) => ({
    panelFreeze: new Set(),
    instrumentFreeze: new Map(),

    freezePanel: (panel: PanelType) => {
        set(state => {
            const newFreeze = new Set(state.panelFreeze);
            newFreeze.add(panel);
            return { panelFreeze: newFreeze };
        });
    },

    resumePanel: (panel: PanelType) => {
        set(state => {
            const newFreeze = new Set(state.panelFreeze);
            newFreeze.delete(panel);
            return { panelFreeze: newFreeze };
        });
    },

    isPanelFrozen: (panel: PanelType) => get().panelFreeze.has(panel),

    freezeInstrument: (panel: PanelType, instrumentKey: string) => {
        set(state => {
            const newMap = new Map(state.instrumentFreeze);
            const panelSet = new Set(newMap.get(panel) ?? []);
            panelSet.add(instrumentKey);
            newMap.set(panel, panelSet);
            return { instrumentFreeze: newMap };
        });
    },

    resumeInstrument: (panel: PanelType, instrumentKey: string) => {
        set(state => {
            const newMap = new Map(state.instrumentFreeze);
            const panelSet = new Set(newMap.get(panel) ?? []);
            panelSet.delete(instrumentKey);
            newMap.set(panel, panelSet);
            return { instrumentFreeze: newMap };
        });
    },

    isInstrumentFrozen: (panel: PanelType, instrumentKey: string) => {
        const state = get();
        return state.instrumentFreeze.get(panel)?.has(instrumentKey) ?? false;
    },

    shouldSkipUpdate: (panel: PanelType, instrumentKey: string) => {
        const state = get();
        // Skip if panel is frozen OR specific instrument is frozen
        return state.panelFreeze.has(panel) ||
            (state.instrumentFreeze.get(panel)?.has(instrumentKey) ?? false);
    },

    freezeAll: () => {
        set({
            panelFreeze: new Set(['orderBook', 'greeks', 'ohlc', 'metrics', 'ltpc'])
        });
    },

    resumeAll: () => {
        set({
            panelFreeze: new Set(),
            instrumentFreeze: new Map()
        });
    }
}));
