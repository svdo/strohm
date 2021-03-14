import Foundation

open class KeyedArrayViewModel<EntryType: ConstructableFromDictionary>: ViewModelBase<[EntryType]> {
    public var sorter: ((EntryType, EntryType) -> Bool)?

    override func propsToData(props: Props) -> [EntryType]? {
        guard let rawData = props[self.propName] as? [String: [String:Any]] else {
            return nil
        }

        var data = rawData.values.compactMap(EntryType.init(from:))
        if let sorter = self.sorter {
            data = data.sorted(by: sorter)
        }
        print("Received entries: ", data)
        return data
    }
}