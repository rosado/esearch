(ns qbits.esearch.utils
  (:require
   [clojure.string :as string]
   [clojure.core.async :as async]
   [cheshire.core :as json]
   [qbits.jet.client.http :as http]))

(defprotocol URLBuilder
  (encode [value]))

(extend-protocol URLBuilder

  clojure.lang.Sequential
  (encode [value]
    (string/join "," (map encode value)))

  clojure.lang.Keyword
  (encode [value]
    (name value))

  Object
  (encode [value] value))

(defn url
  [& parts]
  (->> parts
       (filter identity)
       (map encode)
       (string/join "/")))

(defprotocol ESearchClient
  (-request [client request-params]))

(defn request-async
  [client request-params]
  (http/request client
                (merge {:headers {"Content-Type" "application/json; charset=UTF-8"}
                        :as :json}
                       request-params)))

(extend-protocol ESearchClient
  org.eclipse.jetty.client.HttpClient
  (-request [this request-params]
    (request-async this request-params)))

;;; shape and type of result dependent on client impl.
(defn request
  [client request-params]
  (-request client request-params))
