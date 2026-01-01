import { createContext, useContext, useState, useEffect, ReactNode } from 'react'

interface PageInfo {
    title: string
    shortcut: string
    subtitle?: string
}

interface PageContextType {
    pageInfo: PageInfo
    setPageInfo: (info: PageInfo) => void
}

const defaultPageInfo: PageInfo = {
    title: 'Dashboard',
    shortcut: 'F1',
    subtitle: 'HOME <GO>'
}

const PageContext = createContext<PageContextType>({
    pageInfo: defaultPageInfo,
    setPageInfo: () => { }
})

export function PageProvider({ children }: { children: ReactNode }) {
    const [pageInfo, setPageInfo] = useState<PageInfo>(defaultPageInfo)
    return (
        <PageContext.Provider value={{ pageInfo, setPageInfo }}>
            {children}
        </PageContext.Provider>
    )
}

export function usePageContext() {
    return useContext(PageContext)
}

/**
 * Hook to set page info in TopBar. Call at the start of each page component.
 */
export function usePageTitle(title: string, shortcut: string, subtitle?: string) {
    const { setPageInfo } = usePageContext()
    useEffect(() => {
        setPageInfo({ title, shortcut, subtitle })
    }, [title, shortcut, subtitle, setPageInfo])
}

