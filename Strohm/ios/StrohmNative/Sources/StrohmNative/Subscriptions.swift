import Foundation

class Subscriptions {
    let strohmNative: StrohmNative
    var pendingSubscriptions: [() -> Void]? = []
    var subscribers: [UUID: HandlerFunction] = [:]
    var subscribers2: [UUID: HandlerFunction2] = [:]

    init(strohmNative: StrohmNative) {
        self.strohmNative = strohmNative
        strohmNative.comms.registerHandlerFunction(name: "subscriptionUpdate",
                                                   function: self.subscriptionUpdateHandler)
    }

    func addSubscriber(propsSpec: PropsSpec,
                       handler: @escaping HandlerFunction,
                       completion: @escaping (UUID) -> Void) {
        if pendingSubscriptions != nil {
            pendingSubscriptions!.append({ [weak self] in
                self?.subscribe_(
                    propsSpec: propsSpec,
                    handler: handler,
                    completion: completion)
            })
        } else {
            subscribe_(propsSpec: propsSpec,
                       handler: handler,
                       completion: completion)
        }
    }

    private func subscribe_(propsSpec: PropsSpec,
                            handler: @escaping HandlerFunction,
                            completion: (UUID) -> Void) {
        guard let encodedPropsSpec = strohmNative.comms.encode(object: propsSpec) else {
            return
        }
        let subscriptionId = UUID()
        subscribers[subscriptionId] = handler
        strohmNative.call(method: "strohm_native.flow.subscribe_from_native(\"\(subscriptionId.uuidString)\", \"\(encodedPropsSpec)\")")
        completion(subscriptionId)
    }

    func addSubscriber2(propsSpec: PropsSpec,
                        handler: @escaping HandlerFunction2,
                        completion: @escaping (UUID) -> Void) {
        if pendingSubscriptions != nil {
            pendingSubscriptions!.append({ [weak self] in
                self?.subscribe2_(
                    propsSpec: propsSpec,
                    handler: handler,
                    completion: completion)
            })
        } else {
            subscribe2_(propsSpec: propsSpec,
                        handler: handler,
                        completion: completion)
        }
    }

    private func subscribe2_(propsSpec: PropsSpec,
                             handler: @escaping HandlerFunction2,
                             completion: (UUID) -> Void) {
        guard let encodedPropsSpec = strohmNative.comms.encode(object: propsSpec) else {
            return
        }
        let subscriptionId = UUID()
        subscribers2[subscriptionId] = handler
        strohmNative.call(method: "strohm_native.flow.subscribe_from_native(\"\(subscriptionId.uuidString)\", \"\(encodedPropsSpec)\")")
        completion(subscriptionId)
    }

    func removeSubscriber(subscriptionId: UUID) {
        subscribers.removeValue(forKey: subscriptionId)
        subscribers2.removeValue(forKey: subscriptionId)
        strohmNative.call(method: "strohm_native.flow.unsubscribe_from_native(\"\(subscriptionId.uuidString)\")")
    }

    func effectuatePendingSubscriptions() {
        if let pending = pendingSubscriptions {
            pendingSubscriptions = nil
            for subscriber in pending { subscriber() }
        }
    }

    func subscriptionUpdateHandler(args: JsonComms.Arguments) {
        if let subscriptionIdString = args["subscriptionId"] as? String,
           let subscriptionId = UUID(uuidString: subscriptionIdString),
           let newPropsSerialized = args["new"] as? String {
            if let subscriber = subscribers[subscriptionId],
               let data = newPropsSerialized.data(using: .utf8),
               let newProps = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                subscriber(newProps)
            } else if let subscriber2 = subscribers2[subscriptionId] {
                subscriber2(newPropsSerialized)
            }
        }
    }
}

