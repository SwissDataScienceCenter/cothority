package ch.epfl.dedis.byzcoin;

import ch.epfl.dedis.lib.Sha256id;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.SkipblockId;
import ch.epfl.dedis.byzcoin.transaction.ClientTransaction;
import ch.epfl.dedis.byzcoin.transaction.TxResult;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.lib.proto.ByzCoinProto;
import com.google.protobuf.InvalidProtocolBufferException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * OmniBlock represents the data stored in a skipblock that is relevant to ByzCoin. This data is split in
 * two parts:
 * - header, which contains hashes of the current state and which is hashed in the block
 * - body, which contains the actual transactions and which is not directly hashed in the block
 */
public class Block {
    private ByzCoinProto.DataHeader dataHeader;
    private DataBody dataBody;
    private SkipblockId sbId;

    /**
     * Instantiates a new OmniBlock given a skipblock.
     *
     * @param sb skipblock holding data for an OmniBLock.
     * @throws CothorityCryptoException if there's a problem with the cryptography
     */
    public Block(SkipBlock sb) throws CothorityCryptoException {
        try {
            // TODO: check that it is actually an OmniBlock by looking at the verifiers
            dataHeader = ByzCoinProto.DataHeader.parseFrom(sb.getData());
            dataBody = new DataBody(ByzCoinProto.DataBody.parseFrom(sb.getPayload()));
            sbId = sb.getId();
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCryptoException(e.getMessage());
        }
    }

    /**
     * Constructor for the OmniBlock held in the given Proof.
     * @param p the proof to parse to find the block
     * @throws CothorityCryptoException if there's a problem with the cryptography
     */
    public Block(Proof p) throws CothorityCryptoException {
        // TODO: How do we know that the block in the proof legitimately links back to the
        // skipchain we think it does?
        SkipBlock sb = p.getLatest();
        try {
            // TODO: check that it is actually an OmniBlock by looking at the verifiers
            dataHeader = ByzCoinProto.DataHeader.parseFrom(sb.getData());
            dataBody = new DataBody(ByzCoinProto.DataBody.parseFrom(sb.getPayload()));
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCryptoException(e.getMessage());
        }
    }

    /**
     * @return the root hash of the merkle tree of the global state.
     * @throws CothorityCryptoException if there's a problem with the cryptography
     */
    public Sha256id getTrieRoot() throws CothorityCryptoException {
        return new Sha256id(dataHeader.getTrieroot());
    }

    /**
     * @return the sha256 of all clientTransactions stored in this block.
     * @throws CothorityCryptoException if there's a problem with the cryptography
     */
    public Sha256id getClientTransactionHash() throws CothorityCryptoException {
        return new Sha256id(dataHeader.getClienttransactionhash());
    }

    /**
     * @return the sha256 hash of all state changes created by the clientTransactions in this block.
     * @throws CothorityCryptoException if there's a problem with the cryptography
     */
    public Sha256id getStateChangesHash() throws CothorityCryptoException {
        return new Sha256id(dataHeader.getStatechangeshash());
    }

    /**
     * Returns only accepted ClientTransactions stored in the block. If you also need rejected
     * transactions, then use getTxResults.
     *
     * @return a list of accepted ClientTransactions stored in this block.
     */
    public List<ClientTransaction> getAcceptedClientTransactions(){
        List<ClientTransaction> result = new ArrayList<>();
        dataBody.getTxResults().forEach(txr ->{
            if (txr.isAccepted()) {
                result.add(txr.getClientTransaction());
            }
        });
        return result;
    }

    /**
     * @return the unix-timestamp in nanoseconds of the block creation time.
     */
    public long getTimestampNano() {
        return dataHeader.getTimestamp();
    }

    /**
     * @return A java.time.Instant representing the creation of this block.
     */
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(getTimestampNano() / 1000 / 1000);
    }

    /**
     * Accessor for the transactions and results in the block.
     * @return a list of transactions
     */
    public List<TxResult> getTxResults() {
        return dataBody.getTxResults();
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Block)) {
            return super.equals(obj);
        }
        Block other = (Block) obj;

        return other.sbId.equals(this.sbId);
    }
}
